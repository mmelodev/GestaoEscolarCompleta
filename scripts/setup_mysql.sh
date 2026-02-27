#!/bin/bash
# Script para configurar o banco de dados MySQL no Linux/Mac
# Este script verifica a conexão e cria o banco de dados

echo "========================================"
echo "Configuração do Banco de Dados MySQL"
echo "========================================"
echo ""

# Verificar se MySQL está instalado
if ! command -v mysql &> /dev/null; then
    echo "ERRO: MySQL não encontrado!"
    echo "Por favor, instale o MySQL primeiro."
    echo ""
    echo "Ubuntu/Debian: sudo apt-get install mysql-server"
    echo "macOS: brew install mysql"
    exit 1
fi

echo "MySQL encontrado!"
echo ""

# Solicitar senha do MySQL root
read -sp "Digite a senha do usuário root do MySQL (pressione Enter se não tiver senha): " MYSQL_PASSWORD
echo ""

echo "Tentando conectar ao MySQL..."

# Tentar conectar e criar o banco
if [ -z "$MYSQL_PASSWORD" ]; then
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
else
    mysql -u root -p"$MYSQL_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
fi

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "Banco de dados criado com sucesso!"
    echo "========================================"
    echo ""
    echo "Banco: arirang_db"
    echo "Host: localhost"
    echo "Porta: 3306"
    echo "Usuário: root"
    echo ""
    echo "Próximos passos:"
    echo "1. Se você configurou uma senha, defina a variável de ambiente:"
    echo "   export DB_PASSWORD='sua_senha'"
    echo ""
    echo "2. Execute a aplicação Spring Boot normalmente"
    echo "   O Hibernate criará as tabelas automaticamente."
    echo ""
else
    echo ""
    echo "========================================"
    echo "ERRO ao criar o banco de dados!"
    echo "========================================"
    echo ""
    echo "Possíveis causas:"
    echo "1. Senha incorreta"
    echo "2. MySQL não está rodando"
    echo "3. Usuário root não tem permissões"
    echo ""
    echo "Verifique se o MySQL está rodando:"
    echo "  sudo systemctl status mysql"
    echo "  # ou"
    echo "  sudo service mysql status"
    echo ""
    exit 1
fi
