# Script de compilação com uso mínimo de memória
# Use este script se estiver tendo problemas de memória durante a compilação

Write-Host "=== Compilação Maven com Configuração de Baixa Memória ===" -ForegroundColor Cyan

# Limpar build anterior
Write-Host "`n[1/4] Limpando builds anteriores..." -ForegroundColor Yellow
mvn clean -q

# Compilar apenas as classes principais (sem testes)
Write-Host "[2/4] Compilando classes principais (sem testes)..." -ForegroundColor Yellow
$env:MAVEN_OPTS = "-Xmx512m -Xms128m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC"
mvn compile -DskipTests -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n[3/4] Compilação bem-sucedida!" -ForegroundColor Green
    
    # Compilar testes separadamente (opcional)
    Write-Host "[4/4] Deseja compilar os testes? (S/N)" -ForegroundColor Yellow
    $resposta = Read-Host
    if ($resposta -eq "S" -or $resposta -eq "s") {
        Write-Host "Compilando testes..." -ForegroundColor Yellow
        mvn test-compile -q
    }
    
    Write-Host "`n=== Compilação concluída com sucesso! ===" -ForegroundColor Green
} else {
    Write-Host "`n=== Erro na compilação ===" -ForegroundColor Red
    Write-Host "Sugestões:" -ForegroundColor Yellow
    Write-Host "1. Aumente o arquivo de paginação do Windows" -ForegroundColor White
    Write-Host "2. Feche outros programas que estejam usando muita memória" -ForegroundColor White
    Write-Host "3. Considere adicionar mais RAM física ao sistema" -ForegroundColor White
    exit 1
}
