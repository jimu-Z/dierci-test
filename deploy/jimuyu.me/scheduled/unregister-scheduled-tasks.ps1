$ErrorActionPreference = 'SilentlyContinue'
@('Qiyuan-Auto-01-Nginx', 'Qiyuan-Auto-02-Backend', 'Qiyuan-Auto-03-Cloudflared') | ForEach-Object {
    if (Get-ScheduledTask -TaskName $_ -ErrorAction SilentlyContinue) {
        Unregister-ScheduledTask -TaskName $_ -Confirm:$false
        Write-Host "Removed: $_"
    }
}
Write-Host "Done."
