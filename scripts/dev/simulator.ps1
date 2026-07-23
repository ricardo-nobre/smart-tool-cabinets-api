param(
    [string]$BaseUrl = $(if ($env:STC_BASE_URL) { $env:STC_BASE_URL } else { "" }),
    [string]$CabinetCode = "",
    [string]$ApiKey = "",
    [string]$AuthMethod = "",
    [string]$Credential = ""
)

$ErrorActionPreference = "Stop"

$CabinetCatalog = @(
    [pscustomobject]@{
        Code = "CAB-001"
        Name = "Cabinet 001"
        Location = "Lab A"
        ApiKey = "DEV-CAB-001"
        Tools = @(
            [pscustomobject]@{ TagCode = "TAG-001"; DisplayName = "Demo screwdriver" }
            [pscustomobject]@{ TagCode = "TAG-002"; DisplayName = "Demo wrench" }
            [pscustomobject]@{ TagCode = "TAG-003"; DisplayName = "Demo pliers" }
            [pscustomobject]@{ TagCode = "TAG-004"; DisplayName = "Demo multimeter" }
        )
    }
    [pscustomobject]@{
        Code = "CAB-WRENCH"
        Name = "Wrench Cabinet"
        Location = "Workshop A"
        ApiKey = "DEV-CAB-WRENCH"
        Tools = @(
            [pscustomobject]@{ TagCode = "WRENCH-A"; DisplayName = "Wrench A" }
            [pscustomobject]@{ TagCode = "WRENCH-B"; DisplayName = "Wrench B" }
            [pscustomobject]@{ TagCode = "WRENCH-C"; DisplayName = "Wrench C" }
        )
    }
    [pscustomobject]@{
        Code = "CAB-SCREW"
        Name = "Screwdriver Cabinet"
        Location = "Workshop A"
        ApiKey = "DEV-CAB-SCREW"
        Tools = @(
            [pscustomobject]@{ TagCode = "SCREWDRIVER-A"; DisplayName = "Screwdriver A" }
            [pscustomobject]@{ TagCode = "SCREWDRIVER-B"; DisplayName = "Screwdriver B" }
            [pscustomobject]@{ TagCode = "SCREWDRIVER-C"; DisplayName = "Screwdriver C" }
        )
    }
    [pscustomobject]@{
        Code = "CAB-MEASURE"
        Name = "Measuring Cabinet"
        Location = "Workshop B"
        ApiKey = "DEV-CAB-MEASURE"
        Tools = @(
            [pscustomobject]@{ TagCode = "CALIPER-A"; DisplayName = "Caliper A" }
            [pscustomobject]@{ TagCode = "MULTIMETER-A"; DisplayName = "Multimeter A" }
            [pscustomobject]@{ TagCode = "TAPE-A"; DisplayName = "Tape Measure A" }
        )
    }
)

$script:ResolvedBaseUrl = $null
$script:SelectedCabinetCode = $null
$script:SelectedToolCatalog = @()

function Get-WebErrorMessage {
    param([object]$ErrorRecord)

    if ($ErrorRecord.ErrorDetails -and $ErrorRecord.ErrorDetails.Message) {
        try {
            $body = $ErrorRecord.ErrorDetails.Message | ConvertFrom-Json
            if ($body.message) {
                return $body.message
            }
        } catch {
            return $ErrorRecord.ErrorDetails.Message
        }
    }

    if ($ErrorRecord.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader(
                $ErrorRecord.Exception.Response.GetResponseStream()
            )
            $responseBody = $reader.ReadToEnd()
            $reader.Dispose()
            if ($responseBody) {
                try {
                    $body = $responseBody | ConvertFrom-Json
                    if ($body.message) {
                        return $body.message
                    }
                } catch {
                    return $responseBody
                }
            }
        } catch {
            # Fall back to the web exception message.
        }
    }

    return $ErrorRecord.Exception.Message
}

function Test-BackendCandidate {
    param([string]$CandidateUrl)

    try {
        $response = Invoke-RestMethod `
            -Method "POST" `
            -Uri "$CandidateUrl/api/device/auth" `
            -ContentType "application/json" `
            -Body (@{
                cabinetCode = "CAB-001"
                apiKey = "DEV-CAB-001"
            } | ConvertTo-Json) `
            -TimeoutSec 3

        return [pscustomobject]@{
            Ready = -not [string]::IsNullOrWhiteSpace($response.deviceToken)
            Message = ""
        }
    } catch {
        return [pscustomobject]@{
            Ready = $false
            Message = Get-WebErrorMessage -ErrorRecord $_
        }
    }
}

function Resolve-BaseUrl {
    param([string]$RequestedBaseUrl)

    $candidates = if ([string]::IsNullOrWhiteSpace($RequestedBaseUrl)) {
        @("http://localhost:8080", "http://localhost:18080")
    } else {
        @($RequestedBaseUrl.TrimEnd("/"))
    }

    Write-Host "A verificar API e base de dados..." -ForegroundColor Cyan
    $lastMessages = @{}

    for ($attempt = 1; $attempt -le 10; $attempt++) {
        foreach ($candidate in $candidates) {
            $status = Test-BackendCandidate -CandidateUrl $candidate
            if ($status.Ready) {
                Write-Host "[OK] Sistema pronto em $candidate" -ForegroundColor Green
                return $candidate
            }
            $lastMessages[$candidate] = $status.Message
        }

        if ($attempt -lt 10) {
            Start-Sleep -Seconds 2
        }
    }

    Write-Host ""
    Write-Host "O simulador nao conseguiu ligar a um backend pronto." -ForegroundColor Red
    Write-Host "Confirme esta ordem:"
    Write-Host "  1. scripts\dev\start-local.cmd"
    Write-Host "  2. arrancar o backend"
    Write-Host "  3. esperar por 'Started SmartToolCabinetsApplication'"
    Write-Host "  4. executar novamente o simulador"

    foreach ($candidate in $candidates) {
        $detail = $lastMessages[$candidate]
        if ($detail) {
            if ($detail -match 'relation ".+" does not exist') {
                $detail = "a base de dados ainda nao tem as migracoes aplicadas"
            } elseif ($detail -match "Connection refused|Nao e possivel ligar|Unable to connect") {
                $detail = "a API ou a base de dados nao esta acessivel"
            }
            Write-Host "  $candidate -> $detail" -ForegroundColor DarkGray
        }
    }

    throw "Backend indisponivel ou ainda em inicializacao."
}

function Invoke-JsonApi {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [hashtable]$Headers = @{}
    )

    $request = @{
        Method = $Method
        Uri = "$script:ResolvedBaseUrl$Path"
        Headers = $Headers
        ContentType = "application/json"
    }

    if ($null -ne $Body) {
        $request.Body = $Body | ConvertTo-Json -Depth 10
    }

    try {
        Invoke-RestMethod @request
    } catch {
        $message = Get-WebErrorMessage -ErrorRecord $_
        if ($message -match 'relation ".+" does not exist') {
            $message = "a base de dados ainda nao esta pronta; reinicie o backend e aguarde pelo arranque completo"
        }
        throw "API $Method $Path falhou: $message"
    }
}

function Select-Cabinet {
    param([string]$RequestedCabinetCode)

    if (-not [string]::IsNullOrWhiteSpace($RequestedCabinetCode)) {
        $requestedCode = $RequestedCabinetCode.Trim().ToUpperInvariant()
        $match = @($CabinetCatalog | Where-Object { $_.Code -eq $requestedCode })
        if ($match.Count -ne 1) {
            throw "Armario desconhecido: $RequestedCabinetCode"
        }
        return $match[0]
    }

    while ($true) {
        Write-Host ""
        Write-Host "PASSO 1/3 - Escolher armario" -ForegroundColor Cyan
        for ($index = 0; $index -lt $CabinetCatalog.Count; $index++) {
            $cabinet = $CabinetCatalog[$index]
            Write-Host ("  [{0}] {1,-12} {2} - {3}" -f
                ($index + 1), $cabinet.Code, $cabinet.Name, $cabinet.Location)
        }
        Write-Host "  [0] Terminar"

        $choice = Read-Host "Opcao"
        $number = 0
        if ([int]::TryParse($choice, [ref]$number)) {
            if ($number -eq 0) {
                return $null
            }
            if ($number -ge 1 -and $number -le $CabinetCatalog.Count) {
                return $CabinetCatalog[$number - 1]
            }
        }
        Write-Host "Escolha um numero da lista." -ForegroundColor Yellow
    }
}

function Select-OperatorAuthentication {
    param(
        [string]$RequestedMethod,
        [string]$RequestedCredential
    )

    $method = $RequestedMethod.Trim().ToUpperInvariant()
    if ($method -and $method -notin @("PIN", "NFC")) {
        throw "Metodo de autenticacao invalido: $RequestedMethod"
    }

    while (-not $method) {
        Write-Host ""
        Write-Host "PASSO 2/3 - Identificar operador" -ForegroundColor Cyan
        Write-Host "  [1] Usar PIN de demonstracao"
        Write-Host "  [2] Usar cartao NFC de demonstracao"
        Write-Host "  [0] Voltar aos armarios"

        $choice = Read-Host "Opcao"
        switch ($choice.Trim()) {
            "1" { $method = "PIN" }
            "2" { $method = "NFC" }
            "0" { return $null }
            default { Write-Host "Escolha 1, 2 ou 0." -ForegroundColor Yellow }
        }
    }

    $credential = $RequestedCredential
    if ([string]::IsNullOrWhiteSpace($credential)) {
        $credential = if ($method -eq "PIN") { "1234" } else { "NFC-OP-001" }
    }

    return [pscustomobject]@{
        Method = $method
        Credential = $credential
    }
}

function Connect-Session {
    param(
        [object]$Cabinet,
        [object]$OperatorAuthentication,
        [string]$RequestedApiKey
    )

    $effectiveApiKey = if ([string]::IsNullOrWhiteSpace($RequestedApiKey)) {
        $Cabinet.ApiKey
    } else {
        $RequestedApiKey
    }

    Write-Host ""
    Write-Host "PASSO 3/3 - Autenticacao" -ForegroundColor Cyan
    Write-Host "  Armario $($Cabinet.Code)..." -NoNewline
    $deviceAuth = Invoke-JsonApi -Method "POST" -Path "/api/device/auth" -Body @{
        cabinetCode = $Cabinet.Code
        apiKey = $effectiveApiKey
    }
    Write-Host " OK" -ForegroundColor Green

    Write-Host "  Operador via $($OperatorAuthentication.Method)..." -NoNewline
    $operatorAuth = Invoke-JsonApi -Method "POST" -Path "/api/device/operator-auth" -Headers @{
        Authorization = "Bearer $($deviceAuth.deviceToken)"
    } -Body @{
        cabinetCode = $Cabinet.Code
        method = $OperatorAuthentication.Method
        credential = $OperatorAuthentication.Credential
    }
    Write-Host " OK" -ForegroundColor Green

    Write-Host "  Sessao iniciada para Operator Demo."

    return [pscustomobject]@{
        DeviceHeaders = @{ Authorization = "Bearer $($deviceAuth.deviceToken)" }
        OperatorHeaders = @{ Authorization = "Bearer OPERATOR-TOKEN-DEMO" }
        OperatorId = $operatorAuth.operatorId
        AuthMethod = $OperatorAuthentication.Method
    }
}

function Get-OperatorAssignments {
    param(
        [hashtable]$OperatorHeaders,
        [string]$OperatorId
    )

    $response = Invoke-JsonApi `
        -Method "GET" `
        -Path "/api/operators/$OperatorId/tool-assignments" `
        -Headers $OperatorHeaders

    return @($response.assignments | Where-Object {
        $_.status -eq "ACTIVE" -or $_.status -eq "PENDING_REVIEW"
    })
}

function Get-CurrentState {
    param(
        [hashtable]$OperatorHeaders,
        [string]$OperatorId
    )

    $openAssignments = @(Get-OperatorAssignments `
        -OperatorHeaders $OperatorHeaders `
        -OperatorId $OperatorId)

    $heldTags = @($openAssignments | Where-Object {
        $_.originCabinetCode -eq $script:SelectedCabinetCode
    } | ForEach-Object {
        $_.tagCode
    })

    return [pscustomobject]@{
        Inside = @($script:SelectedToolCatalog | Where-Object {
            $heldTags -notcontains $_.TagCode
        })
        Held = @($script:SelectedToolCatalog | Where-Object {
            $heldTags -contains $_.TagCode
        })
        AllOpenAssignments = $openAssignments
    }
}

function Show-ToolList {
    param(
        [string]$Title,
        [object[]]$Tools
    )

    $items = @($Tools)
    Write-Host ""
    Write-Host $Title -ForegroundColor Cyan
    if ($items.Count -eq 0) {
        Write-Host "  Nenhuma ferramenta disponivel." -ForegroundColor DarkGray
        return
    }

    for ($index = 0; $index -lt $items.Count; $index++) {
        Write-Host ("  [{0}] {1} - {2}" -f
            ($index + 1), $items[$index].TagCode, $items[$index].DisplayName)
    }
}

function Read-ToolSelection {
    param(
        [string]$Title,
        [object[]]$Tools,
        [bool]$AllowEmpty = $false
    )

    $items = @($Tools)
    Show-ToolList -Title $Title -Tools $items
    if ($items.Count -eq 0) {
        return @()
    }

    while ($true) {
        $emptyHint = if ($AllowEmpty) { "Enter = nenhuma" } else { "Enter = cancelar" }
        $raw = Read-Host "Numeros separados por virgula ($emptyHint)"

        if ([string]::IsNullOrWhiteSpace($raw)) {
            return @()
        }

        $indexes = @()
        $valid = $true
        foreach ($part in @($raw -split "[,\s]+" | Where-Object { $_ })) {
            $number = 0
            if (-not [int]::TryParse($part, [ref]$number) -or
                $number -lt 1 -or
                $number -gt $items.Count) {
                $valid = $false
                break
            }
            if ($indexes -notcontains $number) {
                $indexes += $number
            }
        }

        if ($valid -and $indexes.Count -gt 0) {
            return @($indexes | ForEach-Object { $items[$_ - 1] })
        }

        Write-Host "Selecao invalida. Exemplo: 1,3" -ForegroundColor Yellow
    }
}

function Format-ToolTags {
    param([object[]]$Tools)

    $items = @($Tools)
    if ($items.Count -eq 0) {
        return "nenhuma"
    }
    return ($items.TagCode -join ", ")
}

function Confirm-Operation {
    $answer = Read-Host "Confirmar? [S/n]"
    return [string]::IsNullOrWhiteSpace($answer) -or
        $answer.Trim().ToUpperInvariant() -in @("S", "SIM", "Y", "YES")
}

function Invoke-CabinetAccessFlow {
    param(
        [hashtable]$DeviceHeaders,
        [string]$OperatorId,
        [string[]]$BeforeTags,
        [string[]]$AfterTags
    )

    $access = Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses" -Headers $DeviceHeaders -Body @{
        cabinetCode = $script:SelectedCabinetCode
        operatorId = $OperatorId
    }

    Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses/$($access.cabinetAccessId)/snapshots" -Headers $DeviceHeaders -Body @{
        snapshotType = "BEFORE"
        capturedAt = [DateTimeOffset]::UtcNow.ToString("o")
        source = "INTERACTIVE_SIMULATOR"
        observedTags = @($BeforeTags)
    } | Out-Null

    Invoke-JsonApi -Method "POST" -Path "/api/device/cabinet-accesses/$($access.cabinetAccessId)/snapshots" -Headers $DeviceHeaders -Body @{
        snapshotType = "AFTER"
        capturedAt = [DateTimeOffset]::UtcNow.ToString("o")
        source = "INTERACTIVE_SIMULATOR"
        observedTags = @($AfterTags)
    } | Out-Null

    return Invoke-JsonApi `
        -Method "POST" `
        -Path "/api/device/cabinet-accesses/$($access.cabinetAccessId)/close" `
        -Headers $DeviceHeaders
}

function Invoke-MovementWorkflow {
    param(
        [object]$State,
        [object]$Context
    )

    Write-Host ""
    Write-Host "Movimentar ferramentas" -ForegroundColor Cyan
    Write-Host "  [1] Retirar"
    Write-Host "  [2] Devolver"
    Write-Host "  [3] Trocar (retirar e devolver)"
    Write-Host "  [0] Voltar"

    $mode = Read-Host "Opcao"
    if ($mode.Trim() -eq "0") {
        return
    }
    if ($mode.Trim() -notin @("1", "2", "3")) {
        Write-Host "Opcao invalida." -ForegroundColor Yellow
        return
    }

    $removedTools = @()
    $returnedTools = @()

    if ($mode.Trim() -in @("1", "3")) {
        if (@($State.Inside).Count -eq 0) {
            Write-Host "Nao existem ferramentas para retirar neste armario." -ForegroundColor Yellow
            if ($mode.Trim() -eq "1") {
                return
            }
        } else {
            $removedTools = @(Read-ToolSelection `
                -Title "Escolha as ferramentas a RETIRAR" `
                -Tools $State.Inside `
                -AllowEmpty ($mode.Trim() -eq "3"))
        }
    }

    if ($mode.Trim() -in @("2", "3")) {
        if (@($State.Held).Count -eq 0) {
            Write-Host "Nao tem ferramentas deste armario para devolver." -ForegroundColor Yellow
            if ($mode.Trim() -eq "2") {
                return
            }
        } else {
            $returnedTools = @(Read-ToolSelection `
                -Title "Escolha as ferramentas a DEVOLVER" `
                -Tools $State.Held `
                -AllowEmpty ($mode.Trim() -eq "3"))
        }
    }

    if ($removedTools.Count -eq 0 -and $returnedTools.Count -eq 0) {
        Write-Host "Operacao cancelada; nenhuma ferramenta foi alterada." -ForegroundColor Yellow
        return
    }

    Write-Host ""
    Write-Host "Resumo" -ForegroundColor Cyan
    Write-Host "  Armario : $script:SelectedCabinetCode"
    Write-Host "  Retirar : $(Format-ToolTags -Tools $removedTools)"
    Write-Host "  Devolver: $(Format-ToolTags -Tools $returnedTools)"

    if (-not (Confirm-Operation)) {
        Write-Host "Operacao cancelada." -ForegroundColor Yellow
        return
    }

    $beforeTags = @($State.Inside | ForEach-Object { $_.TagCode })
    $removedTags = @($removedTools | ForEach-Object { $_.TagCode })
    $afterTags = @($State.Inside |
        Where-Object { $removedTags -notcontains $_.TagCode } |
        ForEach-Object { $_.TagCode })
    $afterTags += @($returnedTools | ForEach-Object { $_.TagCode })

    $result = Invoke-CabinetAccessFlow `
        -DeviceHeaders $Context.DeviceHeaders `
        -OperatorId $Context.OperatorId `
        -BeforeTags $beforeTags `
        -AfterTags $afterTags

    Write-Host ""
    Write-Host "[OK] Movimento registado." -ForegroundColor Green
    Write-Host "  Retiradas: $($result.assignmentsCreatedCount)"
    Write-Host "  Devolvidas: $($result.assignmentsReturnedCount)"
    if ($result.discrepancyFlag) {
        Write-Host "  Foi detetada uma discrepancia." -ForegroundColor Yellow
    }
}

function Show-OperatorStatus {
    param(
        [hashtable]$OperatorHeaders,
        [string]$OperatorId
    )

    $result = Invoke-JsonApi `
        -Method "GET" `
        -Path "/api/operators/$OperatorId/end-of-day-check" `
        -Headers $OperatorHeaders

    Write-Host ""
    Write-Host "Estado do operador" -ForegroundColor Cyan
    if ($result.pendingAssignmentsCount -eq 0) {
        Write-Host "  [OK] Sem ferramentas pendentes. Pode terminar o dia." -ForegroundColor Green
        return
    }

    Write-Host "  Pendentes: $($result.pendingAssignmentsCount)" -ForegroundColor Yellow
    foreach ($assignment in @($result.pendingAssignments)) {
        Write-Host "  - $($assignment.tagCode) - $($assignment.toolDisplayName)"
        Write-Host "    Armario de origem: $($assignment.originCabinetCode)"
    }
}

function Show-SessionMenu {
    param(
        [object]$Cabinet,
        [object]$Context,
        [object]$State
    )

    Write-Host ""
    Write-Host "============================================================"
    Write-Host "$($Cabinet.Code) - $($Cabinet.Name) | $($Cabinet.Location)" -ForegroundColor Green
    Write-Host "Operator Demo | autenticado por $($Context.AuthMethod)"
    Write-Host ("Dentro: {0}/{1} | Em posse deste armario: {2} | Pendentes totais: {3}" -f
        @($State.Inside).Count,
        @($script:SelectedToolCatalog).Count,
        @($State.Held).Count,
        @($State.AllOpenAssignments).Count)
    Write-Host ""
    Write-Host "  [1] Movimentar ferramentas"
    Write-Host "  [2] Ver estado do operador"
    Write-Host "  [3] Trocar de armario"
    Write-Host "  [0] Terminar simulador"
}

Write-Host ""
Write-Host "SMART TOOL CABINETS - SIMULADOR" -ForegroundColor Green
$script:ResolvedBaseUrl = Resolve-BaseUrl -RequestedBaseUrl $BaseUrl

$requestedCabinetCode = $CabinetCode
$requestedApiKey = $ApiKey
$exitSimulator = $false

while (-not $exitSimulator) {
    $selectedCabinet = Select-Cabinet -RequestedCabinetCode $requestedCabinetCode
    $requestedCabinetCode = ""
    if ($null -eq $selectedCabinet) {
        break
    }

    $operatorAuthentication = Select-OperatorAuthentication `
        -RequestedMethod $AuthMethod `
        -RequestedCredential $Credential
    if ($null -eq $operatorAuthentication) {
        continue
    }

    $script:SelectedCabinetCode = $selectedCabinet.Code
    $script:SelectedToolCatalog = @($selectedCabinet.Tools)

    try {
        $context = Connect-Session `
            -Cabinet $selectedCabinet `
            -OperatorAuthentication $operatorAuthentication `
            -RequestedApiKey $requestedApiKey
        $requestedApiKey = ""
    } catch {
        Write-Host ""
        Write-Host "Nao foi possivel iniciar a sessao." -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor DarkGray
        continue
    }

    $changeCabinet = $false
    while (-not $changeCabinet -and -not $exitSimulator) {
        $state = Get-CurrentState `
            -OperatorHeaders $context.OperatorHeaders `
            -OperatorId $context.OperatorId

        Show-SessionMenu `
            -Cabinet $selectedCabinet `
            -Context $context `
            -State $state

        $choice = Read-Host "Opcao"
        switch ($choice.Trim()) {
            "1" {
                Invoke-MovementWorkflow -State $state -Context $context
            }
            "2" {
                Show-OperatorStatus `
                    -OperatorHeaders $context.OperatorHeaders `
                    -OperatorId $context.OperatorId
            }
            "3" {
                $changeCabinet = $true
            }
            "0" {
                $exitSimulator = $true
            }
            default {
                Write-Host "Escolha 1, 2, 3 ou 0." -ForegroundColor Yellow
            }
        }
    }
}

Write-Host ""
Write-Host "Simulador terminado."
