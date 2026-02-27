# 🔧 Solução Rápida para o Erro "Schema directory already exists"

## Execute no MySQL Workbench (UMA LINHA POR VEZ):

### 1️⃣ Primeiro, execute esta linha:
```sql
DROP DATABASE IF EXISTS arirang_db;
```
**Aguarde a confirmação de sucesso antes de continuar.**

### 2️⃣ Depois, execute esta linha:
```sql
CREATE DATABASE arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3️⃣ Verifique se funcionou:
```sql
SHOW DATABASES LIKE 'arirang_db';
```

Você deve ver `arirang_db` na lista.

---

## ⚠️ Se ainda não funcionar:

1. Feche o MySQL Workbench completamente
2. Abra novamente
3. Execute novamente os comandos acima

---

## ✅ Próximos Passos:

Após criar o banco com sucesso:

1. Se você tem senha no MySQL root, configure:
   ```powershell
   $env:DB_PASSWORD="sua_senha"
   ```

2. Execute sua aplicação Spring Boot normalmente
3. O Hibernate criará as tabelas automaticamente
