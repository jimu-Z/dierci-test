$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Write-Section {
    param([string]$Message)
    Write-Host ''
    Write-Host $Message
    Write-Host ''
}

function Get-JavaVersionLine {
    param([string]$JavaExe)

    $commandLine = '""{0}"" -version 2>&1' -f $JavaExe
    return & cmd /c $commandLine | Select-Object -First 1
}

function Resolve-JavaHome {
    $triedHomes = @()
    $candidates = @()

    if ($env:JAVA_HOME) {
        $candidates += $env:JAVA_HOME
    }

    $candidates += @(
        'E:\Java\jdk-21',
        'C:\Program Files\Java\jdk-21',
        'C:\Program Files\Eclipse Adoptium\jdk-21',
        'C:\Program Files\Zulu\zulu-21',
        'C:\Program Files\Java\jdk-17',
        'C:\Program Files\Eclipse Adoptium\jdk-17'
    )

    foreach ($candidate in $candidates) {
        if (-not $candidate) {
            continue
        }

        $triedHomes += $candidate
        $javaExe = Join-Path $candidate 'bin\java.exe'
        if (Test-Path $javaExe) {
            $versionLine = Get-JavaVersionLine -JavaExe $javaExe
            if ($versionLine -match '"21(\.|")') {
                return (Resolve-Path $candidate).Path
            }
        }
    }

    $javacCommand = Get-Command javac -ErrorAction SilentlyContinue
    if ($javacCommand -and $javacCommand.Source) {
        $candidate = Split-Path (Split-Path $javacCommand.Source -Parent) -Parent
        $javaExe = Join-Path $candidate 'bin\java.exe'
        if (Test-Path $javaExe) {
            $versionLine = Get-JavaVersionLine -JavaExe $javaExe
            if ($versionLine -match '"21(\.|")') {
                return $candidate
            }
        }
    }

    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCommand -and $javaCommand.Source) {
        $candidate = Split-Path (Split-Path $javaCommand.Source -Parent) -Parent
        $javaExe = Join-Path $candidate 'bin\java.exe'
        if (Test-Path $javaExe) {
            $versionLine = Get-JavaVersionLine -JavaExe $javaExe
            if ($versionLine -match '"21(\.|")') {
                return $candidate
            }
        }
    }

    throw "Unable to locate a usable JDK. Set JAVA_HOME or install JDK 21. Tried: $($triedHomes -join ', ')"
}

function Resolve-MavenExecutable {
    $mavenCommand = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mavenCommand -and $mavenCommand.Source) {
        return $mavenCommand.Source
    }

    $wrapper = Join-Path $PSScriptRoot 'mvnw.cmd'
    if (Test-Path $wrapper) {
        return $wrapper
    }

    throw 'Unable to locate Maven. Install mvn on PATH or add mvnw.cmd to the repository root.'
}

function Invoke-MavenChecked {
    param(
        [string]$Label,
        [string]$MavenExecutable,
        [string]$JavaHome,
        [string[]]$Arguments
    )

    $escapedArgs = ($Arguments | ForEach-Object {
        if ($_ -eq $null) {
            '""'
        } else {
            '"{0}"' -f ($_ -replace '"', '\"')
        }
    }) -join ' '

    $commandLine = 'set "JAVA_HOME={0}" && set "PATH={0}\bin;{1}" && call "{2}" {3}' -f $JavaHome, $env:Path, $MavenExecutable, $escapedArgs
    Write-Host $Label
    & cmd /c $commandLine
    if ($LASTEXITCODE -ne 0) {
        throw "$Label failed with exit code $LASTEXITCODE"
    }
}

function Invoke-CheckedCommand {
    param(
        [string]$Label,
        [string]$Executable,
        [string[]]$Arguments
    )

    Write-Host $Label
    & $Executable @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Label failed with exit code $LASTEXITCODE"
    }
}

Push-Location (Join-Path $PSScriptRoot '..')
try {
    $rootDir = (Get-Location).Path
    $adminDir = Join-Path $rootDir 'ruoyi-admin'
    $classpathFile = Join-Path $adminDir 'target\dev-classpath.txt'
    $javaHome = Resolve-JavaHome
    $mavenExe = Resolve-MavenExecutable
    $javaExe = Join-Path $javaHome 'bin\java.exe'

    if (-not (Test-Path $javaExe)) {
        throw "Selected JAVA_HOME does not contain java.exe: $javaHome"
    }

    $env:JAVA_HOME = $javaHome
    $env:Path = "{0}\bin;{1}" -f $javaHome, $env:Path

    Write-Section '[info] direct dev startup, no packaging'
    Write-Host ("[info] JAVA_HOME = {0}" -f $javaHome)
    Write-Host ("[info] Maven = {0}" -f $mavenExe)

    Invoke-MavenChecked '[1/3] compile Maven reactor ...' $mavenExe $javaHome @('-pl', 'ruoyi-admin', '-am', '-DskipTests', 'compile')

    Invoke-MavenChecked '[2/3] generate runtime classpath ...' $mavenExe $javaHome @(
        '-pl', 'ruoyi-admin',
        '-am',
        '-q',
        '-DincludeScope=runtime',
        '-Dmdep.outputFile=target/dev-classpath.txt',
        '-Dmdep.pathSeparator=;',
        'dependency:build-classpath'
    )

    if (-not (Test-Path $classpathFile)) {
        throw "Runtime classpath file not found: $classpathFile"
    }

    $runtimeCp = Get-Content -LiteralPath $classpathFile -Raw
    $runtimeCp = ($runtimeCp -split ';' | Where-Object {
        $_ -and ($_ -notmatch '[\\/]ruoyi-(admin|common|system|framework|quartz|generator)[\\/](target[\\/]classes|[^;]+\.jar)')
    }) -join ';'

    $moduleCp = @(
        (Join-Path $adminDir 'target\classes'),
        (Join-Path $rootDir 'ruoyi-common\target\classes'),
        (Join-Path $rootDir 'ruoyi-system\target\classes'),
        (Join-Path $rootDir 'ruoyi-framework\target\classes'),
        (Join-Path $rootDir 'ruoyi-quartz\target\classes'),
        (Join-Path $rootDir 'ruoyi-generator\target\classes')
    ) -join ';'

    $appClasspath = "$moduleCp;$runtimeCp"

    Write-Host '[3/3] start RuoYiApplication ...'
    $javaArgs = @(
        '-Dspring.profiles.active=druid',
        '-Dfile.encoding=UTF-8',
        '-cp',
        $appClasspath,
        'com.ruoyi.RuoYiApplication'
    )
    & $javaExe @javaArgs
}
finally {
    Pop-Location
}