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
        [string]$CabinetCode = "CAB-001",
        [string]$OperatorId,
        [string[]]$BeforeTags,
        [string[]]$AfterTags
    )

    $access = Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses" -Headers $DeviceHeaders -Body @{
        cabinetCode = $CabinetCode
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

    $itemsArray = @($Items)
    if ($itemsArray.Count -eq 0) {
        Write-Host "No items available."
        return @()
    }

    Write-Host $Title
    for ($i = 0; $i -lt $itemsArray.Count; $i++) {
        $label = & $FormatItem $itemsArray[$i]
        Write-Host "  $($i + 1). $label"
    }
    Write-Host "  0. Exit"

    $input = Read-Host "Choose numbers separated by comma"
    if ([string]::IsNullOrWhiteSpace($input) -or $input.Trim() -eq "0") {
        return @()
    }

    $selected = @()
    foreach ($part in $input.Split(",")) {
        $index = 0
        if ([int]::TryParse($part.Trim(), [ref]$index)) {
            if ($index -ge 1 -and $index -le $itemsArray.Count) {
                $selected += $itemsArray[$index - 1]
            }
        }
    }
    return @($selected)
}

function Get-InteractiveCabinets {
    @(
        [pscustomobject]@{
            Code = "CAB-WRENCH"
            ApiKey = "DEV-CAB-WRENCH"
            Name = "Wrench Cabinet"
            Tools = @(
                [pscustomobject]@{ Tag = "WRENCH-A"; Name = "Wrench A" },
                [pscustomobject]@{ Tag = "WRENCH-B"; Name = "Wrench B" },
                [pscustomobject]@{ Tag = "WRENCH-C"; Name = "Wrench C" }
            )
            PresentTags = @("WRENCH-A", "WRENCH-B", "WRENCH-C")
            DeviceHeaders = $null
        },
        [pscustomobject]@{
            Code = "CAB-SCREW"
            ApiKey = "DEV-CAB-SCREW"
            Name = "Screwdriver Cabinet"
            Tools = @(
                [pscustomobject]@{ Tag = "SCREWDRIVER-A"; Name = "Screwdriver A" },
                [pscustomobject]@{ Tag = "SCREWDRIVER-B"; Name = "Screwdriver B" },
                [pscustomobject]@{ Tag = "SCREWDRIVER-C"; Name = "Screwdriver C" }
            )
            PresentTags = @("SCREWDRIVER-A", "SCREWDRIVER-B", "SCREWDRIVER-C")
            DeviceHeaders = $null
        },
        [pscustomobject]@{
            Code = "CAB-MEASURE"
            ApiKey = "DEV-CAB-MEASURE"
            Name = "Measuring Cabinet"
            Tools = @(
                [pscustomobject]@{ Tag = "CALIPER-A"; Name = "Caliper A" },
                [pscustomobject]@{ Tag = "MULTIMETER-A"; Name = "Multimeter A" },
                [pscustomobject]@{ Tag = "TAPE-A"; Name = "Tape Measure A" }
            )
            PresentTags = @("CALIPER-A", "MULTIMETER-A", "TAPE-A")
            DeviceHeaders = $null
        }
    )
}

function Get-ToolLabel {
    param(
        [object[]]$Cabinets,
        [string]$Tag
    )

    foreach ($cabinet in $Cabinets) {
        foreach ($tool in $cabinet.Tools) {
            if ($tool.Tag -eq $Tag) {
                return "$($tool.Name) [$Tag]"
            }
        }
    }
    return $Tag
}

function Get-CabinetForTag {
    param(
        [object[]]$Cabinets,
        [string]$Tag
    )

    foreach ($cabinet in $Cabinets) {
        foreach ($tool in $cabinet.Tools) {
            if ($tool.Tag -eq $Tag) {
                return $cabinet
            }
        }
    }
    return $null
}

function Start-InteractiveDay {
    param([object[]]$Cabinets)

    Write-Host "Starting day..."
    foreach ($cabinet in $Cabinets) {
        $auth = Invoke-JsonApi -Method "POST" -Path "/api/device/auth" -Body @{
            cabinetCode = $cabinet.Code
            apiKey = $cabinet.ApiKey
        }
        $cabinet.DeviceHeaders = @{ Authorization = "Bearer $($auth.deviceToken)" }
        Write-Host "Authenticated $($cabinet.Name) ($($cabinet.Code))"
    }

    $operatorAuth = Invoke-JsonApi -Method "POST" -Path "/api/device/operator-auth" -Headers $Cabinets[0].DeviceHeaders -Body @{
        cabinetCode = $Cabinets[0].Code
        method = "PIN"
        credential = "1234"
    }

    [pscustomobject]@{
        OperatorId = $operatorAuth.operatorId
        OperatorHeaders = @{ Authorization = "Bearer OPERATOR-TOKEN-DEMO" }
        SupervisorHeaders = @{ Authorization = "Bearer SUPERVISOR-TOKEN-DEMO" }
        Cabinets = $Cabinets
    }
}

function Show-AdminFunctions {
    param([object[]]$Cabinets)

    Write-Host ""
    Write-Host "Admin functions"
    Write-Host "Demo setup currently available:"
    foreach ($cabinet in $Cabinets) {
        Write-Host "  $($cabinet.Name) ($($cabinet.Code))"
        foreach ($tool in $cabinet.Tools) {
            Write-Host "    - $($tool.Name) [$($tool.Tag)]"
        }
    }
    Write-Host ""
    Write-Host "These are seeded through Flyway V2 for the simulator."
}

function Show-DayState {
    param([object]$Context)

    Write-Host ""
    Write-Host "Cabinet state"
    foreach ($cabinet in $Context.Cabinets) {
        Write-Host "  $($cabinet.Name): $($cabinet.PresentTags -join ', ')"
    }

    Write-Host ""
    Write-Host "Operator assignments"
    Show-Json (Get-Assignments -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId)
}

function Invoke-PickTools {
    param([object]$Context)

    $selectedCabinet = Select-FromList `
        -Title "Choose cabinet" `
        -Items $Context.Cabinets `
        -FormatItem { param($cabinet) "$($cabinet.Name) ($($cabinet.Code))" }

    if ($selectedCabinet.Count -eq 0) {
        return
    }

    $cabinet = $selectedCabinet[0]
    Write-Host "Opening $($cabinet.Name)..."

    $availableTools = @($cabinet.Tools | Where-Object { $cabinet.PresentTags -contains $_.Tag })
    $selectedTools = Select-FromList `
        -Title "Pick tools from $($cabinet.Name)" `
        -Items $availableTools `
        -FormatItem { param($tool) "$($tool.Name) [$($tool.Tag)]" }

    if ($selectedTools.Count -eq 0) {
        Write-Host "Closing $($cabinet.Name) without changes."
        return
    }

    $pickedTags = @($selectedTools | ForEach-Object { $_.Tag })
    $beforeTags = @($cabinet.PresentTags)
    $afterTags = @($beforeTags | Where-Object { $pickedTags -notcontains $_ })

    $flow = Invoke-CabinetAccessFlow `
        -DeviceHeaders $cabinet.DeviceHeaders `
        -CabinetCode $cabinet.Code `
        -OperatorId $Context.OperatorId `
        -BeforeTags $beforeTags `
        -AfterTags $afterTags

    $cabinet.PresentTags = $afterTags

    Write-Host "Exit pressed. Cabinet closed."
    Write-Host "Picked: $($pickedTags -join ', ')"
    Write-Host "Close result: $($flow.Close.operationalResult)"
    Write-Host "Assignments created: $($flow.Close.assignmentsCreatedCount)"
}

function Invoke-ReturnTools {
    param(
        [object]$Context,
        [switch]$All
    )

    $active = @(Get-ActiveAssignments -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId)
    if ($active.Count -eq 0) {
        Write-Host "No active tools to return."
        return
    }

    $selectedAssignments = $active
    if (-not $All) {
        $selectedAssignments = Select-FromList `
            -Title "Choose tools to return" `
            -Items $active `
            -FormatItem { param($assignment) "$($assignment.toolDisplayName) [$($assignment.tagCode)]" }
    }

    if ($selectedAssignments.Count -eq 0) {
        return
    }

    $routableAssignments = @()
    foreach ($assignment in $selectedAssignments) {
        $cabinetForTool = Get-CabinetForTag -Cabinets $Context.Cabinets -Tag $assignment.tagCode
        if ($null -eq $cabinetForTool) {
            Write-Host "Skipping $($assignment.tagCode): no simulator cabinet maps this tool."
            continue
        }

        $routableAssignments += [pscustomobject]@{
            CabinetCode = $cabinetForTool.Code
            Assignment = $assignment
        }
    }

    if ($routableAssignments.Count -eq 0) {
        Write-Host "No selected tools can be returned by this interactive simulator."
        return
    }

    $groups = $routableAssignments | Group-Object {
        $_.CabinetCode
    }

    foreach ($group in $groups) {
        $cabinet = $Context.Cabinets | Where-Object { $_.Code -eq $group.Name } | Select-Object -First 1
        if ($null -eq $cabinet) {
            Write-Host "No cabinet found for group $($group.Name)"
            continue
        }

        $returnTags = @($group.Group | ForEach-Object { $_.Assignment.tagCode })
        $beforeTags = @($cabinet.PresentTags)
        $afterTags = @($beforeTags + $returnTags | Select-Object -Unique)

        Write-Host "Opening $($cabinet.Name) to return $($returnTags -join ', ')..."
        $flow = Invoke-CabinetAccessFlow `
            -DeviceHeaders $cabinet.DeviceHeaders `
            -CabinetCode $cabinet.Code `
            -OperatorId $Context.OperatorId `
            -BeforeTags $beforeTags `
            -AfterTags $afterTags

        $cabinet.PresentTags = $afterTags
        Write-Host "Exit pressed. Cabinet closed."
        Write-Host "Return result: $($flow.Close.operationalResult)"
        Write-Host "Assignments returned: $($flow.Close.assignmentsReturnedCount)"
    }
}

function Invoke-WorkPeriod {
    param([string]$Label)

    Write-Host ""
    Write-Host "$Label..."
    Start-Sleep -Seconds 2
}

function Invoke-SupervisorReview {
    param([object]$Context)

    $active = @(Get-ActiveAssignments -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId)
    $selectedAssignments = Select-FromList `
        -Title "Choose broken/missing tool for supervisor review" `
        -Items $active `
        -FormatItem { param($assignment) "$($assignment.toolDisplayName) [$($assignment.tagCode)]" }

    if ($selectedAssignments.Count -eq 0) {
        return
    }

    $reasonCode = Read-Host "Reason code [BROKEN_TOOL]"
    if ([string]::IsNullOrWhiteSpace($reasonCode)) {
        $reasonCode = "BROKEN_TOOL"
    }

    $reportText = Read-Host "Supervisor note"
    if ([string]::IsNullOrWhiteSpace($reportText)) {
        $reportText = "Tool marked for supervisor review by interactive simulator."
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
}

function Invoke-LunchPrompt {
    param([object]$Context)

    Invoke-WorkPeriod -Label "Working until lunch"
    $answer = Read-Host "It is lunch time. Drop off some tools? [y/N]"
    if ($answer -match "^[Yy]") {
        Invoke-ReturnTools -Context $Context
    } else {
        Write-Host "No tools returned at lunch."
    }
}

function Invoke-EndOfDay {
    param([object]$Context)

    Invoke-WorkPeriod -Label "Working until end of day"
    $answer = Read-Host "End of day. Return all active tools? [Y/n]"
    if ($answer -notmatch "^[Nn]") {
        Invoke-ReturnTools -Context $Context -All
    }

    $endOfDay = Get-EndOfDay -OperatorHeaders $Context.OperatorHeaders -OperatorId $Context.OperatorId
    Write-Host "End-of-day check:"
    Show-Json $endOfDay

    if ($endOfDay.pendingAssignmentsCount -gt 0) {
        $answer = Read-Host "Pending tools remain. Open supervisor review? [y/N]"
        if ($answer -match "^[Yy]") {
            Invoke-SupervisorReview -Context $Context
        }
    }
}

function Invoke-InteractiveScenario {
    $cabinets = @(Get-InteractiveCabinets)
    $context = $null

    Write-Host "Interactive workday simulator"
    Write-Host "Tip: run scripts\dev\reset-db.cmd before this for a clean demo."

    while ($null -eq $context) {
        Write-Host ""
        Write-Host "1. Start day"
        Write-Host "2. Admin functions"
        Write-Host "0. Exit"

        $choice = Read-Host "Choose option"
        try {
            switch ($choice) {
                "1" {
                    $context = Start-InteractiveDay -Cabinets $cabinets
                    Write-Host "Day started for operator $($context.OperatorId)"
                }
                "2" {
                    Show-AdminFunctions -Cabinets $cabinets
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

    while ($true) {
        Write-Host ""
        Write-Host "1. Go pick up tools"
        Write-Host "2. Go back to work"
        Write-Host "3. Lunch time"
        Write-Host "4. End of day return"
        Write-Host "5. Tool broken/missing review"
        Write-Host "6. Show current state"
        Write-Host "0. Exit"

        $choice = Read-Host "Choose option"
        try {
            switch ($choice) {
                "1" { Invoke-PickTools -Context $context }
                "2" { Invoke-WorkPeriod -Label "Working" }
                "3" { Invoke-LunchPrompt -Context $context }
                "4" { Invoke-EndOfDay -Context $context }
                "5" { Invoke-SupervisorReview -Context $context }
                "6" { Show-DayState -Context $context }
                "0" { return }
                default { Write-Host "Invalid option." }
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
