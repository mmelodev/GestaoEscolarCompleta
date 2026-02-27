# Como Configurar a Senha do MySQL

## ⚠️ Erro Atual

```
Access denied for user 'root'@'localhost' (using password: NO)
```

Isso significa que o MySQL root **requer senha**, mas a aplicação não está enviando.

## 🔧 Solução: Configurar Variável de Ambiente

### Opção 1: PowerShell (Temporário - Apenas para esta sessão)

Abra o PowerShell na pasta do projeto e execute:

```powershell
$env:DB_PASSWORD="sua_senha_aqui"
```

Depois execute a aplicação normalmente.

### Opção 2: CMD (Temporário - Apenas para esta sessão)

Abra o CMD na pasta do projeto e execute:

```cmd
set DB_PASSWORD=sua_senha_aqui
```

Depois execute a aplicação normalmente.

### Opção 3: Tornar Permanente (Recomendado)

Para não precisar configurar toda vez:

**Windows PowerShell (como Administrador):**
```powershell
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "sua_senha_aqui", "User")
```

**Windows CMD (como Administrador):**
```cmd
setx DB_PASSWORD "sua_senha_aqui"
```

⚠️ **IMPORTANTE:** Após usar `setx`, feche e reabra o terminal/IDE para a variável ser carregada.

### Opção 4: Configurar no IntelliJ IDEA / VS Code

#### IntelliJ IDEA:
1. Vá em **Run → Edit Configurations**
2. Selecione sua configuração de execução
3. Em **Environment variables**, adicione:
   - Nome: `DB_PASSWORD`
   - Valor: `sua_senha_aqui`
4. Clique em **Apply** e **OK**

#### VS Code:
1. Crie ou edite o arquivo `.vscode/launch.json`
2. Adicione:
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Spring Boot App",
            "request": "launch",
            "mainClass": "br.com.arirang.plataforma.PlataformaApplication",
            "env": {
                "DB_PASSWORD": "sua_senha_aqui"
            }
        }
    ]
}
```

## 🔍 Verificar se Funcionou

Após configurar, execute a aplicação. Se ainda der erro, verifique:

1. **A senha está correta?**
   - Teste no MySQL Workbench com a mesma senha

2. **A variável está configurada?**
   ```powershell
   # PowerShell
   echo $env:DB_PASSWORD
   
   # CMD
   echo %DB_PASSWORD%
   ```

3. **O MySQL está rodando?**
   ```powershell
   Get-Service -Name MySQL*
   ```

## 🎯 Solução Rápida (Teste)

Para testar rapidamente, execute no PowerShell:

```powershell
# Substitua "sua_senha" pela senha real do seu MySQL root
$env:DB_PASSWORD="sua_senha"
cd D:\Desktop\codespace\arirang\Arirang-plataforma
mvn spring-boot:run
```

Ou se estiver usando IDE, configure a variável de ambiente antes de executar.

## 📝 Nota sobre Segurança

⚠️ **NUNCA** coloque a senha diretamente no arquivo `application-dev.properties`!

A configuração atual usa variável de ambiente por segurança:
```properties
spring.datasource.password=${DB_PASSWORD:}
```

Isso significa:
- Se `DB_PASSWORD` existir → usa a variável de ambiente
- Se não existir → usa string vazia (sem senha)

## ✅ Próximos Passos

1. Configure a variável `DB_PASSWORD` com a senha do seu MySQL root
2. Execute a aplicação novamente
3. O Hibernate criará as tabelas automaticamente
