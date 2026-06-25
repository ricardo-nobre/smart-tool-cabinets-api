param(
    [string]$BaseUrl = $(if ($env:STC_BASE_URL) { $env:STC_BASE_URL } else { "http://localhost:8080" })
)

$ErrorActionPreference = "Stop"

function Invoke-JsonApi {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body,
        [hashtable]$Headers = @{}
    )

    $json = $null
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 10
    }

    Invoke-RestMethod `
        -Method $Method `
        -Uri "$BaseUrl$Path" `
        -Headers $Headers `
        -ContentType "application/json" `
        -Body $json
}

Write-Host "Running normal-flow against $BaseUrl"

$deviceAuth = Invoke-JsonApi -Method "POST" -Path "/api/device/auth" -Body @{
    cabinetCode = "CAB-001"
    apiKey = "DEV-CAB-001"
}
$deviceHeaders = @{ Authorization = "Bearer $($deviceAuth.deviceToken)" }

$operatorAuth = Invoke-JsonApi -Method "POST" -Path "/api/device/operator-auth" -Headers $deviceHeaders -Body @{
    cabinetCode = "CAB-001"
    method = "PIN"
    credential = "1234"
}

$access = Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses" -Headers $deviceHeaders -Body @{
    cabinetCode = "CAB-001"
    operatorId = $operatorAuth.operatorId
}

Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses/$($access.cabinetAccessId)/snapshots" -Headers $deviceHeaders -Body @{
    snapshotType = "BEFORE"
    capturedAt = [DateTimeOffset]::UtcNow.ToString("o")
    source = "SIMULATOR"
    observedTags = @("TAG-001", "TAG-002", "TAG-003")
} | Out-Null

Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses/$($access.cabinetAccessId)/snapshots" -Headers $deviceHeaders -Body @{
    snapshotType = "AFTER"
    capturedAt = [DateTimeOffset]::UtcNow.ToString("o")
    source = "SIMULATOR"
    observedTags = @("TAG-001", "TAG-003")
} | Out-Null

$close = Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses/$($access.cabinetAccessId)/close" -Headers $deviceHeaders

$operatorHeaders = @{ Authorization = "Bearer OPERATOR-TOKEN-DEMO" }
$assignments = Invoke-JsonApi -Method "GET" -Path "/api/operators/$($operatorAuth.operatorId)/tool-assignments" -Headers $operatorHeaders

Write-Host "CabinetAccess: $($access.cabinetAccessId)"
Write-Host "Close result: $($close.operationalResult)"
Write-Host "Assignments created: $($close.assignmentsCreatedCount)"
$assignments | ConvertTo-Json -Depth 10
