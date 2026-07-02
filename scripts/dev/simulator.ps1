param(
    [ValidateSet("normal", "return-tool", "missing-tool", "all", "interactive")]
    [string]$Scenario = "normal",
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
        SupervisorHeaders = @{ Authorization = "Bearer SUPERVISOR-TOKEN-DEMO" }
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

function Get-Assignments {
    param(
        [hashtable]$OperatorHeaders,
        [string]$OperatorId
    )

    Invoke-JsonApi -Method "GET" -Path "/api/operators/$OperatorId/tool-assignments" -Headers $OperatorHeaders
}

function Get-EndOfDay {
    param(
        [hashtable]$OperatorHeaders,
        [string]$OperatorId
    )

    Invoke-JsonApi -Method "GET" -Path "/api/operators/$OperatorId/end-of-day-check" -Headers $OperatorHeaders
}

function Show-Json {
    param([object]$Value)

    $Value | ConvertTo-Json -Depth 10
}

function Invoke-NormalScenario {
    param([object]$Context)

    Write-Host "Scenario: normal checkout"
    Write-Host "BEFORE: TAG-001, TAG-002, TAG-003"
    Write-Host "AFTER : TAG-001, TAG-003"

    $flow = Invoke-CabinetAccessFlow `
        -DeviceHeaders $Context.DeviceHeaders `
        -OperatorId $Context.OperatorId `
        -BeforeTags @("TAG-001", "TAG-002", "TAG-003") `
        -AfterTags @("TAG-001", "TAG-003")

    Assert-Value "operationalResult" $flow.Close.operationalResult "CLOSED_WITH_ASSIGNMENTS"
    Assert-Value "assignmentsCreatedCount" $flow.Close.assignmentsCreatedCount 1

    Write-Host "CabinetAccess: $($flow.Access.cabinetAccessId)"
    Write-Host "Close result: $($flow.Close.operationalResult)"
    Write-Host "Assignments created: $($flow.Close.assignmentsCreatedCount)"
    Show-Json (Get-Assignments -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId)
}

function Invoke-ReturnToolScenario {
    param([object]$Context)

    Write-Host "Scenario: checkout and return"
    Write-Host "Checkout BEFORE: TAG-001, TAG-003"
    Write-Host "Checkout AFTER : TAG-001"
    Write-Host "Return BEFORE  : TAG-001"
    Write-Host "Return AFTER   : TAG-001, TAG-003"

    $checkout = Invoke-CabinetAccessFlow `
        -DeviceHeaders $Context.DeviceHeaders `
        -OperatorId $Context.OperatorId `
        -BeforeTags @("TAG-001", "TAG-003") `
        -AfterTags @("TAG-001")

    Assert-Value "checkout operationalResult" $checkout.Close.operationalResult "CLOSED_WITH_ASSIGNMENTS"
    Assert-Value "checkout assignmentsCreatedCount" $checkout.Close.assignmentsCreatedCount 1

    Write-Host "Checkout CabinetAccess: $($checkout.Access.cabinetAccessId)"
    Write-Host "Checkout result: $($checkout.Close.operationalResult)"
    Write-Host "Assignments created: $($checkout.Close.assignmentsCreatedCount)"
    Write-Host "Assignments after checkout:"
    Show-Json (Get-Assignments -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId)

    $return = Invoke-CabinetAccessFlow `
        -DeviceHeaders $Context.DeviceHeaders `
        -OperatorId $Context.OperatorId `
        -BeforeTags @("TAG-001") `
        -AfterTags @("TAG-001", "TAG-003")

    Assert-Value "return operationalResult" $return.Close.operationalResult "CLOSED_WITH_ASSIGNMENTS"
    Assert-Value "return assignmentsReturnedCount" $return.Close.assignmentsReturnedCount 1

    Write-Host "Return CabinetAccess: $($return.Access.cabinetAccessId)"
    Write-Host "Return result: $($return.Close.operationalResult)"
    Write-Host "Assignments returned: $($return.Close.assignmentsReturnedCount)"
    Write-Host "Assignments after return:"
    Show-Json (Get-Assignments -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId)
}

function Invoke-MissingToolScenario {
    param([object]$Context)

    Write-Host "Scenario: missing tool and supervisor resolution"
    Write-Host "BEFORE: TAG-001, TAG-003, TAG-004"
    Write-Host "AFTER : TAG-001, TAG-003"

    $flow = Invoke-CabinetAccessFlow `
        -DeviceHeaders $Context.DeviceHeaders `
        -OperatorId $Context.OperatorId `
        -BeforeTags @("TAG-001", "TAG-003", "TAG-004") `
        -AfterTags @("TAG-001", "TAG-003")

    Assert-Value "operationalResult" $flow.Close.operationalResult "CLOSED_WITH_ASSIGNMENTS"
    Assert-Value "assignmentsCreatedCount" $flow.Close.assignmentsCreatedCount 1

    $endOfDay = Get-EndOfDay -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId

    Write-Host "CabinetAccess: $($flow.Access.cabinetAccessId)"
    Write-Host "Close result: $($flow.Close.operationalResult)"
    Write-Host "Pending assignments before resolution: $($endOfDay.pendingAssignmentsCount)"

    if ($endOfDay.pendingAssignmentsCount -gt 0) {
        $assignmentIds = @($endOfDay.pendingAssignments | ForEach-Object { $_.assignmentId })
        $resolution = Invoke-JsonApi -Method "POST" -Path "/api/supervisor/resolutions" -Headers $Context.SupervisorHeaders -Body @{
            operatorId = $Context.OperatorId
            supervisorId = "00000000-0000-0000-0000-000000000301"
            reasonCode = "DEMO_RESOLUTION"
            reportText = "Supervisor resolution created by simulator."
            decisionAt = [DateTimeOffset]::UtcNow.ToString("o")
            allowExit = $true
            assignmentIds = $assignmentIds
        }
        Write-Host "SupervisorResolution: $($resolution.resolutionId)"
    }

    $endOfDayAfterResolution = Get-EndOfDay -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId
    Assert-Value "pendingAssignmentsCount after resolution" $endOfDayAfterResolution.pendingAssignmentsCount 0

    Write-Host "Pending assignments after resolution: $($endOfDayAfterResolution.pendingAssignmentsCount)"
    Show-Json $endOfDayAfterResolution
}

function Get-ActiveAssignments {
    param(
        [hashtable]$OperatorHeaders,
        [string]$OperatorId
    )

    $assignments = Get-Assignments -OperatorHeaders $OperatorHeaders -OperatorId $OperatorId
    @($assignments.assignments | Where-Object { $_.status -eq "ACTIVE" })
}

function Select-FromList {
    param(
        [string]$Title,
        [object[]]$Items,
        [scriptblock]$FormatItem
    )

    if ($Items.Count -eq 0) {
        Write-Host "No items available."
        return @()
    }

    Write-Host $Title
    for ($i = 0; $i -lt $Items.Count; $i++) {
        $label = & $FormatItem $Items[$i]
        Write-Host "  $($i + 1). $label"
    }

    $input = Read-Host "Choose numbers separated by comma, or press Enter to cancel"
    if ([string]::IsNullOrWhiteSpace($input)) {
        return @()
    }

    $selected = New-Object System.Collections.Generic.List[object]
    foreach ($part in $input.Split(",")) {
        $index = 0
        if ([int]::TryParse($part.Trim(), [ref]$index)) {
            if ($index -ge 1 -and $index -le $Items.Count) {
                $selected.Add($Items[$index - 1])
            }
        }
    }
    return @($selected)
}

function Show-InteractiveStatus {
    param(
        [object]$Context,
        [string[]]$CabinetTags
    )

    Write-Host ""
    Write-Host "Cabinet CAB-001 visible tags: $($CabinetTags -join ', ')"
    if ($null -ne $Context) {
        Write-Host "Operator: $($Context.OperatorId)"
        $assignments = Get-Assignments -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId
        Write-Host "Assignments:"
        Show-Json $assignments
    } else {
        Write-Host "Operator: not authenticated"
    }
}

function Invoke-InteractiveCheckout {
    param(
        [object]$Context,
        [ref]$CabinetTags,
        [hashtable]$ToolNames
    )

    $availableTags = @($CabinetTags.Value)
    $selectedTags = Select-FromList `
        -Title "Tools currently in cabinet" `
        -Items $availableTags `
        -FormatItem { param($tag) "$tag - $($ToolNames[$tag])" }

    if ($selectedTags.Count -eq 0) {
        return
    }

    $beforeTags = @($CabinetTags.Value)
    $afterTags = @($beforeTags | Where-Object { $selectedTags -notcontains $_ })

    $flow = Invoke-CabinetAccessFlow `
        -DeviceHeaders $Context.DeviceHeaders `
        -OperatorId $Context.OperatorId `
        -BeforeTags $beforeTags `
        -AfterTags $afterTags

    $CabinetTags.Value = $afterTags

    Write-Host "Picked: $($selectedTags -join ', ')"
    Write-Host "Close result: $($flow.Close.operationalResult)"
    Write-Host "Assignments created: $($flow.Close.assignmentsCreatedCount)"
    Show-Json (Get-Assignments -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId)
}

function Invoke-InteractiveReturn {
    param(
        [object]$Context,
        [ref]$CabinetTags,
        [switch]$All
    )

    $active = @(Get-ActiveAssignments -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId)
    if ($active.Count -eq 0) {
        Write-Host "No active assignments to return."
        return
    }

    $selectedAssignments = $active
    if (-not $All) {
        $selectedAssignments = Select-FromList `
            -Title "Active assignments" `
            -Items $active `
            -FormatItem { param($assignment) "$($assignment.tagCode) - $($assignment.toolDisplayName)" }
    }

    if ($selectedAssignments.Count -eq 0) {
        return
    }

    $returnTags = @($selectedAssignments | ForEach-Object { $_.tagCode })
    $beforeTags = @($CabinetTags.Value)
    $afterTags = @($beforeTags + $returnTags | Select-Object -Unique)

    $flow = Invoke-CabinetAccessFlow `
        -DeviceHeaders $Context.DeviceHeaders `
        -OperatorId $Context.OperatorId `
        -BeforeTags $beforeTags `
        -AfterTags $afterTags

    $CabinetTags.Value = $afterTags

    Write-Host "Returned: $($returnTags -join ', ')"
    Write-Host "Close result: $($flow.Close.operationalResult)"
    Write-Host "Assignments returned: $($flow.Close.assignmentsReturnedCount)"
    Show-Json (Get-Assignments -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId)
}

function Invoke-InteractiveReview {
    param([object]$Context)

    $active = @(Get-ActiveAssignments -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId)
    $selectedAssignments = Select-FromList `
        -Title "Choose assignments for supervisor review" `
        -Items $active `
        -FormatItem { param($assignment) "$($assignment.tagCode) - $($assignment.toolDisplayName)" }

    if ($selectedAssignments.Count -eq 0) {
        return
    }

    Write-Host "Reason examples: BROKEN_TOOL, TOOL_NOT_RETURNED, LOST_TOOL"
    $reasonCode = Read-Host "Reason code"
    if ([string]::IsNullOrWhiteSpace($reasonCode)) {
        $reasonCode = "BROKEN_TOOL"
    }

    $reportText = Read-Host "Supervisor note"
    if ([string]::IsNullOrWhiteSpace($reportText)) {
        $reportText = "Resolved by interactive simulator."
    }

    $assignmentIds = @($selectedAssignments | ForEach-Object { $_.assignmentId })
    $resolution = Invoke-JsonApi -Method "POST" -Path "/api/supervisor/resolutions" -Headers $Context.SupervisorHeaders -Body @{
        operatorId = $Context.OperatorId
        supervisorId = "00000000-0000-0000-0000-000000000301"
        reasonCode = $reasonCode.Trim().ToUpperInvariant()
        reportText = $reportText.Trim()
        decisionAt = [DateTimeOffset]::UtcNow.ToString("o")
        allowExit = $true
        assignmentIds = $assignmentIds
    }

    Write-Host "SupervisorResolution: $($resolution.resolutionId)"
    Show-Json (Get-EndOfDay -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId)
}

function Invoke-InteractiveScenario {
    $toolNames = @{
        "TAG-001" = "Demo screwdriver"
        "TAG-002" = "Demo wrench"
        "TAG-003" = "Demo pliers"
        "TAG-004" = "Demo multimeter"
    }
    $cabinetTags = @("TAG-001", "TAG-002", "TAG-003", "TAG-004")
    $context = $null

    Write-Host "Interactive day simulator"
    Write-Host "Tip: run scripts\dev\reset-db.cmd before this for a clean demo."

    while ($true) {
        Write-Host ""
        Write-Host "1. Start day: authenticate cabinet and operator"
        Write-Host "2. Go to cabinet and pick tools"
        Write-Host "3. Lunch time: return selected tools"
        Write-Host "4. End of day: return all active tools"
        Write-Host "5. Tool broken/missing: supervisor review"
        Write-Host "6. Show current state"
        Write-Host "0. Exit"

        $choice = Read-Host "Choose option"

        try {
            switch ($choice) {
                "1" {
                    $context = Connect-DemoDevice
                    Write-Host "Authenticated operator $($context.OperatorId)"
                }
                "2" {
                    if ($null -eq $context) { $context = Connect-DemoDevice }
                    Invoke-InteractiveCheckout -Context $context -CabinetTags ([ref]$cabinetTags) -ToolNames $toolNames
                }
                "3" {
                    if ($null -eq $context) { $context = Connect-DemoDevice }
                    Invoke-InteractiveReturn -Context $context -CabinetTags ([ref]$cabinetTags)
                }
                "4" {
                    if ($null -eq $context) { $context = Connect-DemoDevice }
                    Invoke-InteractiveReturn -Context $context -CabinetTags ([ref]$cabinetTags) -All
                    Show-Json (Get-EndOfDay -OperatorHeaders $context.OperatorHeaders -OperatorId $context.OperatorId)
                }
                "5" {
                    if ($null -eq $context) { $context = Connect-DemoDevice }
                    Invoke-InteractiveReview -Context $context
                }
                "6" {
                    Show-InteractiveStatus -Context $context -CabinetTags $cabinetTags
                }
                "0" {
                    return
                }
                default {
                    Write-Host "Invalid option."
                }
            }
        } catch {
            Write-Host "Error: $($_.Exception.Message)"
        }
    }
}

Write-Host "Running simulator scenario '$Scenario' against $BaseUrl"

if ($Scenario -eq "interactive") {
    Invoke-InteractiveScenario
    return
}

$context = Connect-DemoDevice

switch ($Scenario) {
    "normal" { Invoke-NormalScenario -Context $context }
    "return-tool" { Invoke-ReturnToolScenario -Context $context }
    "missing-tool" { Invoke-MissingToolScenario -Context $context }
    "all" {
        Invoke-NormalScenario -Context $context
        Write-Host ""
        Invoke-ReturnToolScenario -Context $context
        Write-Host ""
        Invoke-MissingToolScenario -Context $context
    }
}
