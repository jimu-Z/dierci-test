param(
    [Parameter(Mandatory = $true)]
    [string] $IngestToken,
    [string] $PublicWebhookBaseUrl = 'https://hook.jimuyu.me',
    [string] $WebhookValidUntil = '2027-05-06'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$env:AGRI_SENSOR_INGEST_TOKEN = $IngestToken
$env:AGRI_EMQX_PUBLIC_WEBHOOK_BASE_URL = $PublicWebhookBaseUrl
$env:AGRI_EMQX_WEBHOOK_VALID_UNTIL = $WebhookValidUntil

& (Join-Path $PSScriptRoot 'run-dev.ps1')
