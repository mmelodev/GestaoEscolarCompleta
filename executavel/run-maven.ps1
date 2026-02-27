# Script PowerShell puro para executar Maven Wrapper
# Este script evita problemas com espacos no caminho do mvnw.cmd

param(
    [Parameter(ValueFromRemainingArguments=$true)]
    [string[]]$MavenArgs
)

$ErrorActionPreference = "Stop"

# Obter diretorio do script
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Carregar propriedades do wrapper
$wrapperPropsPath = Join-Path $scriptDir ".mvn\wrapper\maven-wrapper.properties"
if (-not (Test-Path $wrapperPropsPath)) {
    Write-Error "cannot read distributionUrl property in $wrapperPropsPath"
    exit 1
}

$props = Get-Content -Raw $wrapperPropsPath | ConvertFrom-StringData
$distributionUrl = $props.distributionUrl

if (!$distributionUrl) {
    Write-Error "cannot read distributionUrl property in $wrapperPropsPath"
    exit 1
}

# Determinar tipo de Maven
$USE_MVND = $false
$MVN_CMD = "mvn.cmd"

if ($distributionUrl -match "maven-mvnd-") {
    $USE_MVND = $true
    $distributionUrl = $distributionUrl -replace '-bin\.[^.]*$',"-windows-amd64.zip"
    $MVN_CMD = "mvnd.cmd"
}

# Aplicar MVNW_REPOURL se configurado
if ($env:MVNW_REPOURL) {
    $MVNW_REPO_PATTERN = if ($USE_MVND) { "/org/apache/maven/" } else { "/maven/mvnd/" }
    $distributionUrl = "$env:MVNW_REPOURL$MVNW_REPO_PATTERN$($distributionUrl -replace '^.*'+$MVNW_REPO_PATTERN,'')"
}

# Calcular MAVEN_HOME
$distributionUrlName = $distributionUrl -replace '^.*/',''
$distributionUrlNameMain = $distributionUrlName -replace '\.[^.]*$','' -replace '-bin$',''

# Usar $HOME do PowerShell (funciona no Windows e Linux)
$HOME_PATH = if ($env:USERPROFILE) { $env:USERPROFILE } else { $env:HOME }
$MAVEN_HOME_PARENT = "$HOME_PATH\.m2\wrapper\dists\$distributionUrlNameMain"

if ($env:MAVEN_USER_HOME) {
    $MAVEN_HOME_PARENT = "$env:MAVEN_USER_HOME\wrapper\dists\$distributionUrlNameMain"
}

$MAVEN_HOME_NAME = ([System.Security.Cryptography.MD5]::Create().ComputeHash([byte[]][char[]]$distributionUrl) | ForEach-Object {$_.ToString("x2")}) -join ''
$MAVEN_HOME = "$MAVEN_HOME_PARENT\$MAVEN_HOME_NAME"
$mvnCmdPath = Join-Path $MAVEN_HOME "bin\$MVN_CMD"

# Se Maven nao existe, baixar
if (-not (Test-Path $mvnCmdPath)) {
    Write-Host "Maven nao encontrado. Baixando..." -ForegroundColor Yellow
    
    # Criar diretorio temporario
    $TMP_DOWNLOAD_DIR = New-Item -ItemType Directory -Path "$env:TEMP\maven-wrapper-$(Get-Random)" -Force
    
    try {
        # Criar diretorio pai
        New-Item -ItemType Directory -Path $MAVEN_HOME_PARENT -Force | Out-Null
        
        # Baixar Maven
        Write-Host "Baixando de: $distributionUrl" -ForegroundColor Cyan
        $downloadPath = Join-Path $TMP_DOWNLOAD_DIR $distributionUrlName
        $webclient = New-Object System.Net.WebClient
        if ($env:MVNW_USERNAME -and $env:MVNW_PASSWORD) {
            $webclient.Credentials = New-Object System.Net.NetworkCredential($env:MVNW_USERNAME, $env:MVNW_PASSWORD)
        }
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        $webclient.DownloadFile($distributionUrl, $downloadPath)
        
        # Validar SHA-256 se especificado
        if ($props.distributionSha256Sum) {
            if ($USE_MVND) {
                Write-Error "Checksum validation is not supported for maven-mvnd"
                exit 1
            }
            $hash = (Get-FileHash $downloadPath -Algorithm SHA256).Hash.ToLower()
            if ($hash -ne $props.distributionSha256Sum.ToLower()) {
                Write-Error "Error: Failed to validate Maven distribution SHA-256"
                exit 1
            }
        }
        
        # Extrair
        Write-Host "Extraindo..." -ForegroundColor Cyan
        Expand-Archive $downloadPath -DestinationPath $TMP_DOWNLOAD_DIR -Force | Out-Null
        
        # Mover para destino final
        $extractedDir = Join-Path $TMP_DOWNLOAD_DIR $distributionUrlNameMain
        if (Test-Path $extractedDir) {
            Move-Item -Path $extractedDir -Destination $MAVEN_HOME -Force
        } else {
            Write-Error "Failed to find extracted Maven directory"
            exit 1
        }
        
        Write-Host "Maven instalado com sucesso!" -ForegroundColor Green
    } finally {
        # Limpar diretorio temporario
        if (Test-Path $TMP_DOWNLOAD_DIR) {
            Remove-Item $TMP_DOWNLOAD_DIR -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}

# Executar Maven
if (Test-Path $mvnCmdPath) {
    # Passar argumentos separadamente para o Maven
    & $mvnCmdPath @MavenArgs
    exit $LASTEXITCODE
} else {
    Write-Error "Maven executable not found at: $mvnCmdPath"
    exit 1
}
