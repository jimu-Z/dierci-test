# 本地验证 EMQX Webhook 解析链（需后端已启动、设备已注册）
$token = $env:AGRI_SENSOR_INGEST_TOKEN
if ([string]::IsNullOrWhiteSpace($token)) {
    Write-Error "请先设置环境变量 AGRI_SENSOR_INGEST_TOKEN"
    exit 1
}
$body = @{
    topic   = "agri/PLOT-A01/DEV-TEMP-001/telemetry"
    payload = @{
        temperature = 26.3
        humidity    = 63.8
        co2         = 512
        timestamp   = [int][DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
    }
} | ConvertTo-Json -Depth 5

Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8080/agri/envSensor/ingest/emqx" `
    -Headers @{ "X-Agri-Token" = $token; "Content-Type" = "application/json" } `
    -Body $body
