$ErrorActionPreference = 'Stop'

Set-Location (Join-Path $PSScriptRoot '..')

$rootDir = (Get-Location).Path
$adminDir = Join-Path $rootDir 'ruoyi-admin'
$classpathFile = Join-Path $adminDir 'target\dev-classpath.txt'
$javaHome = 'E:\Java\jdk-21'

$env:JAVA_HOME = $javaHome
$env:Path = "{0}\bin;{1}" -f $javaHome, $env:Path

Write-Host ''
Write-Host '[info] direct dev startup, no packaging'
Write-Host ''

Write-Host '[1/3] compile Maven reactor ...'
& mvn -pl ruoyi-admin -am -DskipTests compile
if ($LASTEXITCODE -ne 0) {
    throw 'compile failed'
}

Write-Host '[2/3] generate runtime classpath ...'
& mvn '-pl' 'ruoyi-admin' '-am' '-q' '-DincludeScope=runtime' '-Dmdep.outputFile=target/dev-classpath.txt' '-Dmdep.pathSeparator=;' 'dependency:build-classpath'
if ($LASTEXITCODE -ne 0) {
    throw 'classpath generation failed'
}

if (-not (Test-Path $classpathFile)) {
    throw "runtime classpath file not found: $classpathFile"
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
& java @javaArgs