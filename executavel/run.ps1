# Script para executar a aplicacao com variaveis de ambiente
# Uso: .\run.ps1

Write-Host "=== Carregando variaveis de ambiente ===" -ForegroundColor Cyan

# Verificar se o arquivo .env existe
if (Test-Path ".env") {
    Write-Host "Arquivo .env encontrado. Carregando variaveis..." -ForegroundColor Green
    Get-Content .env | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
            Write-Host "  OK $name configurado" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "AVISO: Arquivo .env nao encontrado!" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "ERRO: Arquivo .env e necessario para executar a aplicacao!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Para criar o arquivo .env:" -ForegroundColor Cyan
    Write-Host "1. Copie o arquivo env.example para .env" -ForegroundColor White
    Write-Host "2. Edite o arquivo .env e configure suas credenciais" -ForegroundColor White
    Write-Host "3. NUNCA commite o arquivo .env no Git!" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Consulte SETUP_ENV.md ou env.example para mais informacoes" -ForegroundColor Cyan
    exit 1
}

Write-Host ""
Write-Host "=== Verificando variaveis obrigatorias ===" -ForegroundColor Cyan

$required = @("DB_PASSWORD", "JWT_SECRET")
$missing = @()

foreach ($var in $required) {
    if ([string]::IsNullOrEmpty([System.Environment]::GetEnvironmentVariable($var, "Process"))) {
        $missing += $var
        Write-Host "  ERRO $var nao configurado" -ForegroundColor Red
    } else {
        Write-Host "  OK $var configurado" -ForegroundColor Green
    }
}

if ($missing.Count -gt 0) {
    Write-Host ""
    Write-Host "ERRO: Variaveis obrigatorias nao configuradas!" -ForegroundColor Red
    Write-Host "Configure as seguintes variaveis:" -ForegroundColor Yellow
    foreach ($var in $missing) {
        Write-Host "  - $var" -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Host "Consulte SETUP_ENV.md para instrucoes." -ForegroundColor Cyan
    exit 1
}

Write-Host ""
Write-Host "=== Iniciando aplicacao Spring Boot ===" -ForegroundColor Cyan
Write-Host ""

# Obter o diretorio atual (resolvendo caminho completo)
$currentDir = (Get-Location).Path

# Verificar se o Maven Wrapper existe
$mvnwPath = Join-Path $currentDir "mvnw.cmd"
$mvnwUnixPath = Join-Path $currentDir "mvnw"

if (Test-Path $mvnwPath) {
    Write-Host "Usando Maven Wrapper..." -ForegroundColor Green
    # Usar script PowerShell puro que evita problemas com espacos no caminho
    $mavenWrapperScript = Join-Path $currentDir "run-maven.ps1"
    Set-Location $currentDir
    if (Test-Path $mavenWrapperScript) {
        & $mavenWrapperScript spring-boot:run
        exit $LASTEXITCODE
    } else {
        # Fallback: tentar usar caminho curto
        Write-Host "Script run-maven.ps1 nao encontrado. Tentando metodo alternativo..." -ForegroundColor Yellow
        $fso = New-Object -ComObject Scripting.FileSystemObject
        $folder = $fso.GetFolder($currentDir)
        $shortPath = $folder.ShortPath
        cmd.exe /c "cd /d `"$shortPath`" && mvnw.cmd spring-boot:run"
        exit $LASTEXITCODE
    }
} elseif (Test-Path $mvnwUnixPath) {
    Write-Host "Usando Maven Wrapper (mvnw)..." -ForegroundColor Green
    Set-Location $currentDir
    & $mvnwUnixPath spring-boot:run
} else {
    Write-Host "Tentando usar Maven global..." -ForegroundColor Yellow
    try {
        $mvnCmd = Get-Command mvn -ErrorAction Stop
        & $mvnCmd spring-boot:run
    } catch {
        Write-Host ""
        Write-Host "ERRO: Maven nao encontrado!" -ForegroundColor Red
        Write-Host "Instale o Maven ou use o Maven Wrapper (mvnw)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Opcoes:" -ForegroundColor Cyan
        Write-Host "1. Instale o Maven: https://maven.apache.org/download.cgi" -ForegroundColor White
        Write-Host "2. Ou use o Maven Wrapper que ja vem no projeto" -ForegroundColor White
        exit 1
    }
}
