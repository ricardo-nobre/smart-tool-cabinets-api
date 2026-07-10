param(
    [string]$BaseUrl = $(if ($env:STC_BASE_URL) { $env:STC_BASE_URL } else { "http://localhost:8080" })
)

$ErrorActionPreference = "Stop"

function Invoke-JsonApi {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
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

function Assert-Value {
    param(
        [string]$Label,
        [object]$Actual,
        [object]$Expected
    )

    if ($Actual -ne $Expected) {
        throw "$Label expected '$Expected' but got '$Actual'"
    }
}

function Show-Json {
    param([object]$Value)

    $Value | ConvertTo-Json -Depth 10
}

function Connect-DemoDevice {
    $deviceAuth = Invoke-JsonApi -Method "POST" -Path "/api/device/auth" -Body @{
        cabinetCode = "CAB-001"
        apiKey = "DEV-CAB-001"
    }

    $operatorAuth = Invoke-JsonApi -Method "POST" -Path "/api/device/operator-auth" -Headers @{
        Authorization = "Bearer $($deviceAuth.deviceToken)"
    } -Body @{
        cabinetCode = "CAB-001"
        method = "PIN"
        credential = "1234"
    }

    [pscustomobject]@{
        DeviceHeaders = @{ Authorization = "Bearer $($deviceAuth.deviceToken)" }
        OperatorHeaders = @{ Authorization = "Bearer OPERATOR-TOKEN-DEMO" }
        OperatorId = $operatorAuth.operatorId
    }
}

function Invoke-CabinetAccessFlow {
    param(
        [hashtable]$DeviceHeaders,
        [string]$OperatorId,
        [string[]]$BeforeTags,
        [string[]]$AfterTags
    )

    $access = Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses" -Headers $DeviceHeaders -Body @{
        cabinetCode = "CAB-001"
        operatorId = $OperatorId
    }

    Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses/$($access.cabinetAccessId)/snapshots" -Headers $DeviceHeaders -Body @{
        snapshotType = "BEFORE"
        capturedAt = [DateTimeOffset]::UtcNow.ToString("o")
        source = "SIMULATOR"
        observedTags = $BeforeTags
    } | Out-Null

    Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses/$($access.cabinetAccessId)/snapshots" -Headers $DeviceHeaders -Body @{
        snapshotType = "AFTER"
        capturedAt = [DateTimeOffset]::UtcNow.ToString("o")
        source = "SIMULATOR"
        observedTags = $AfterTags
    } | Out-Null

    $close = Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses/$($access.cabinetAccessId)/close" -Headers $DeviceHeaders

    [pscustomobject]@{
        Access = $access
        Close = $close
    }
}

function Get-EndOfDay {
    param(
        [hashtable]$OperatorHeaders,
        [string]$OperatorId
    )

    Invoke-JsonApi -Method "GET" -Path "/api/operators/$OperatorId/end-of-day-check" -Headers $OperatorHeaders
}

Write-Host "Running simulator against $BaseUrl"
Write-Host "Scenario: working day checkout, exchange and final return"

$context = Connect-DemoDevice

Write-Host "[1] Cabinet and operator authenticated"
Write-Host "[2] Opening first CabinetAccess for checkout"
Write-Host "[3] BEFORE snapshot: TAG-001, TAG-002, TAG-003"
Write-Host "[4] AFTER snapshot : TAG-001, TAG-003"

$checkout = Invoke-CabinetAccessFlow `
    -DeviceHeaders $context.DeviceHeaders `
    -OperatorId $context.OperatorId `
    -BeforeTags @("TAG-001", "TAG-002", "TAG-003") `
    -AfterTags @("TAG-001", "TAG-003")

Assert-Value "checkout operationalResult" $checkout.Close.operationalResult "CLOSED_WITH_ASSIGNMENTS"
Assert-Value "checkout assignmentsCreatedCount" $checkout.Close.assignmentsCreatedCount 1

Write-Host "[OK] Checkout CabinetAccess closed: $($checkout.Access.cabinetAccessId)"
Write-Host "[OK] TAG-002 assigned to operator as ACTIVE"

$afterCheckout = Get-EndOfDay -OperatorHeaders $context.OperatorHeaders -OperatorId $context.OperatorId
Assert-Value "pendingAssignmentsCount after checkout" $afterCheckout.pendingAssignmentsCount 1
Write-Host "[OK] End-of-day check detects 1 pending assignment before exchange"

Write-Host "[5] Opening second CabinetAccess for tool exchange"
Write-Host "[6] BEFORE snapshot: TAG-001, TAG-003, TAG-004"
Write-Host "[7] AFTER snapshot : TAG-001, TAG-002, TAG-003"

$exchange = Invoke-CabinetAccessFlow `
    -DeviceHeaders $context.DeviceHeaders `
    -OperatorId $context.OperatorId `
    -BeforeTags @("TAG-001", "TAG-003", "TAG-004") `
    -AfterTags @("TAG-001", "TAG-002", "TAG-003")

Assert-Value "exchange operationalResult" $exchange.Close.operationalResult "CLOSED_WITH_ASSIGNMENTS"
Assert-Value "exchange assignmentsReturnedCount" $exchange.Close.assignmentsReturnedCount 1
Assert-Value "exchange assignmentsCreatedCount" $exchange.Close.assignmentsCreatedCount 1

Write-Host "[OK] Exchange CabinetAccess closed: $($exchange.Access.cabinetAccessId)"
Write-Host "[OK] TAG-002 marked as RETURNED"
Write-Host "[OK] TAG-004 assigned to operator as ACTIVE"

$afterExchange = Get-EndOfDay -OperatorHeaders $context.OperatorHeaders -OperatorId $context.OperatorId
Assert-Value "pendingAssignmentsCount after exchange" $afterExchange.pendingAssignmentsCount 1
Write-Host "[OK] End-of-day check now detects TAG-004 as the pending assignment"

Write-Host "[8] Opening third CabinetAccess for final return"
Write-Host "[9] BEFORE snapshot: TAG-001, TAG-002, TAG-003"
Write-Host "[10] AFTER snapshot : TAG-001, TAG-002, TAG-003, TAG-004"

$finalReturn = Invoke-CabinetAccessFlow `
    -DeviceHeaders $context.DeviceHeaders `
    -OperatorId $context.OperatorId `
    -BeforeTags @("TAG-001", "TAG-002", "TAG-003") `
    -AfterTags @("TAG-001", "TAG-002", "TAG-003", "TAG-004")

Assert-Value "final return operationalResult" $finalReturn.Close.operationalResult "CLOSED_WITH_ASSIGNMENTS"
Assert-Value "final return assignmentsReturnedCount" $finalReturn.Close.assignmentsReturnedCount 1

Write-Host "[OK] Final return CabinetAccess closed: $($finalReturn.Access.cabinetAccessId)"
Write-Host "[OK] TAG-004 marked as RETURNED"

Write-Host "[11] Running final end-of-day-check"
$finalEndOfDay = Get-EndOfDay -OperatorHeaders $context.OperatorHeaders -OperatorId $context.OperatorId
Assert-Value "final pendingAssignmentsCount" $finalEndOfDay.pendingAssignmentsCount 0
Assert-Value "final allowExit" $finalEndOfDay.allowExit $true

Write-Host "[OK] No pending assignments"
Write-Host "[OK] Operator can exit"
Show-Json $finalEndOfDay
