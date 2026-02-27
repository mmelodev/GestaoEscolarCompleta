@echo off
REM Script para configurar o banco de dados MySQL no Windows
REM Este script verifica a conexão e cria o banco de dados

echo ========================================
echo Configuracao do Banco de Dados MySQL
echo ========================================
echo.

REM Verificar se MySQL está instalado e acessível
echo Verificando conexao com MySQL...
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: MySQL nao encontrado no PATH!
    echo Por favor, certifique-se de que o MySQL esta instalado e configurado.
    echo.
    echo Instrucoes:
    echo 1. Instale o MySQL Server se ainda nao tiver
    echo 2. Adicione o MySQL ao PATH do Windows
    echo    (geralmente: C:\Program Files\MySQL\MySQL Server 8.0\bin)
    pause
    exit /b 1
)

echo MySQL encontrado!
echo.

REM Solicitar senha do MySQL root
set /p MYSQL_PASSWORD="Digite a senha do usuario root do MySQL (pressione Enter se nao tiver senha): "

echo.
echo Tentando conectar ao MySQL...

REM Tentar conectar e criar o banco
if "%MYSQL_PASSWORD%"=="" (
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
) else (
    mysql -u root -p%MYSQL_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
)

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo Banco de dados criado com sucesso!
    echo ========================================
    echo.
    echo Banco: arirang_db
    echo Host: localhost
    echo Porta: 3306
    echo Usuario: root
    echo.
    echo Próximos passos:
    echo 1. Se voce configurou uma senha, defina a variavel de ambiente:
    echo    setx DB_PASSWORD "sua_senha"
    echo.
    echo 2. Execute a aplicacao Spring Boot normalmente
    echo    O Hibernate criara as tabelas automaticamente.
    echo.
) else (
    echo.
    echo ========================================
    echo ERRO ao criar o banco de dados!
    echo ========================================
    echo.
    echo Possiveis causas:
    echo 1. Senha incorreta
    echo 2. MySQL nao esta rodando
    echo 3. Usuario root nao tem permissoes
    echo.
    echo Verifique se o MySQL esta rodando:
    echo   - Abra o MySQL Workbench
    echo   - Ou verifique nos servicos do Windows (Services.msc)
    echo.
)

pause
