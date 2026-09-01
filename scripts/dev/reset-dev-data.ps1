[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('THIS WILL DELETE LOCAL DEV DATA')]
    [string]$Confirmation
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$resetSql = Join-Path $PSScriptRoot 'reset-dev-data.sql'
$seedSql = Join-Path $PSScriptRoot 'seed-dev-data.sql'
$containerResetSql = '/tmp/sitiopro-reset-dev-data.sql'
$containerSeedSql = '/tmp/sitiopro-seed-dev-data.sql'
$expectedDatabase = 'sitio_db'
$sqlConfirmation = 'LOCAL_DEV_RESET_CONFIRMED'
$appWasStopped = $false

function Invoke-DockerCompose {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Arguments)

    & docker compose @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose falhou: $($Arguments -join ' ')"
    }
}

Push-Location $projectRoot
try {
    Write-Warning 'THIS WILL DELETE LOCAL DEV DATA'

    $runtime = & docker compose exec -T app sh -lc 'printf "%s|%s|%s" "$SPRING_PROFILES_ACTIVE" "$DB_HOST" "$DB_NAME"'
    if ($LASTEXITCODE -ne 0) {
        throw 'O container app precisa estar em execucao para validar o ambiente DEV ativo.'
    }

    $runtimeParts = ($runtime | Out-String).Trim().Split('|')
    if (($runtimeParts.Count -ne 3) -or
            ($runtimeParts[0] -ne 'dev') -or
            ($runtimeParts[1] -ne 'sqlserver') -or
            ($runtimeParts[2] -ne $expectedDatabase)) {
        throw 'Reset recusado: o runtime ativo nao corresponde ao Docker DEV local esperado.'
    }

    Invoke-DockerCompose cp $resetSql "sqlserver:$containerResetSql"
    Invoke-DockerCompose cp $seedSql "sqlserver:$containerSeedSql"
    Invoke-DockerCompose stop app
    $appWasStopped = $true

    $sqlcmd = '/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -b -r 1 -d sitio_db'
    $resetCommand = "$sqlcmd -i $containerResetSql -v DevResetConfirmation=$sqlConfirmation ExpectedDatabase=$expectedDatabase"
    $seedCommand = "$sqlcmd -i $containerSeedSql -v ExpectedDatabase=$expectedDatabase"
    Invoke-DockerCompose exec -T sqlserver bash -lc $resetCommand
    Invoke-DockerCompose exec -T sqlserver bash -lc $seedCommand
} finally {
    & docker compose exec -T --user root sqlserver rm -f $containerResetSql $containerSeedSql 2>$null
    if ($appWasStopped) {
        & docker compose up -d --no-deps app
    }
    Pop-Location
}

Write-Host 'Reset e seed DEV concluidos. Aguarde o healthcheck da aplicacao antes do uso.'
