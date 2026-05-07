# 以当前用户注册 3 个「登录时」任务（需能读写计划任务；一般无需管理员）
# 用法：在 PowerShell 中执行：
#   Set-ExecutionPolicy -Scope CurrentUser RemoteSigned -Force   # 若被策略拦截，仅首次
#   cd F:\chuangye\deploy\jimuyu.me\scheduled
#   .\register-scheduled-tasks.ps1

$ErrorActionPreference = 'Stop'
$ScheduledDir = $PSScriptRoot
$UserId = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name

$tasks = @(
    @{
        Name        = 'Qiyuan-Auto-01-Nginx'
        Description = '登录后启动 Nginx 8890（若依静态前端）。依赖 launchers\paths.bat'
        Bat         = Join-Path $ScheduledDir 'at-logon-01-nginx.bat'
        DelaySec    = 0
    },
    @{
        Name        = 'Qiyuan-Auto-02-Backend'
        Description = '登录后延迟 30s 启动若依后端 8080。依赖 paths.bat 与 ingest-token.txt'
        Bat         = Join-Path $ScheduledDir 'at-logon-02-backend.bat'
        DelaySec    = 0
    },
    @{
        Name        = 'Qiyuan-Auto-03-Cloudflared'
        Description = '登录后启动 Cloudflare Tunnel（app/hook）。依赖 paths.bat'
        Bat         = Join-Path $ScheduledDir 'at-logon-03-cloudflared.bat'
        DelaySec    = 0
    }
)

foreach ($t in $tasks) {
    if (-not (Test-Path -LiteralPath $t.Bat)) {
        throw "Missing file: $($t.Bat)"
    }
    $existing = Get-ScheduledTask -TaskName $t.Name -ErrorAction SilentlyContinue
    if ($existing) {
        Unregister-ScheduledTask -TaskName $t.Name -Confirm:$false
    }

    $arg = '/c ""{0}""' -f $t.Bat
    $action = New-ScheduledTaskAction -Execute 'cmd.exe' -Argument $arg -WorkingDirectory $ScheduledDir

    # 当前用户登录时触发（延迟在各自 .bat 内用 timeout 实现）
    $trigger = New-ScheduledTaskTrigger -AtLogOn -User $UserId

    $settings = New-ScheduledTaskSettingsSet `
        -AllowStartIfOnBatteries `
        -DontStopIfGoingOnBatteries `
        -StartWhenAvailable `
        -ExecutionTimeLimit ([TimeSpan]::Zero) `
        -MultipleInstances IgnoreNew

    $principal = New-ScheduledTaskPrincipal -UserId $UserId -LogonType Interactive -RunLevel Limited

    Register-ScheduledTask `
        -TaskName $t.Name `
        -Action $action `
        -Trigger $trigger `
        -Settings $settings `
        -Principal $principal `
        -Description $t.Description | Out-Null

    Write-Host "Registered: $($t.Name)"
}

Write-Host ""
Write-Host "Done. Ensure these exist:"
Write-Host "  deploy/jimuyu.me/launchers/paths.bat (copy from paths.example.bat)"
Write-Host "  deploy/jimuyu.me/launchers/ingest-token.txt (line 1 = token)"
Write-Host ""
Write-Host "To remove tasks: .\unregister-scheduled-tasks.ps1"
