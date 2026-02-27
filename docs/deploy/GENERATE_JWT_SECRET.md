# Como Gerar um JWT Secret Seguro

Este documento explica como gerar um JWT Secret seguro para uso na aplicação.

## Requisitos

- **Tamanho mínimo**: 32 caracteres (256 bits)
- **Complexidade**: Recomenda-se usar uma combinação de letras, números e caracteres especiais
- **Aleatoriedade**: Use um gerador seguro de números aleatórios

## Métodos para Gerar

### 1. PowerShell (Windows)

```powershell
# Gerar um secret de 32 bytes (256 bits) em Base64
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))

# Ou gerar uma string aleatória de 64 caracteres
-join ((48..57) + (65..90) + (97..122) + (33..47) | Get-Random -Count 64 | ForEach-Object {[char]$_})
```

### 2. OpenSSL (Linux/Mac)

```bash
# Gerar um secret de 32 bytes em Base64
openssl rand -base64 32

# Ou gerar uma string hexadecimal
openssl rand -hex 32
```

### 3. Online (Use com cuidado!)

- https://www.random.org/strings/
  - Configurar: 32 caracteres, alfanumérico + símbolos
- https://generate-secret.vercel.app/32

### 4. Node.js

```javascript
require('crypto').randomBytes(32).toString('base64')
```

### 5. Python

```python
import secrets
import base64
base64.b64encode(secrets.token_bytes(32)).decode('utf-8')
```

## Exemplo de Secret Seguro

```
Kx9mP2qR8sT5vW1yZ3aB6cD9eF4gH7jK0lM3nO6pQ2rS5tU8vW1xY4zA7bC
```

## Configuração

Depois de gerar o secret, configure-o como variável de ambiente:

### Windows (PowerShell - Temporário)

```powershell
$env:JWT_SECRET="seu_secret_aqui"
```

### Windows (PowerShell - Permanente)

```powershell
[System.Environment]::SetEnvironmentVariable('JWT_SECRET', 'seu_secret_aqui', 'User')
```

### Linux/Mac

```bash
export JWT_SECRET="seu_secret_aqui"
```

Ou adicione ao arquivo `.env`:

```
JWT_SECRET=seu_secret_aqui
```

## Validação

A aplicação validará automaticamente:

- ✅ Secret configurado (obrigatório em produção)
- ✅ Tamanho mínimo de 32 caracteres
- ✅ Complexidade (em produção)

## Segurança

⚠️ **IMPORTANTE**:

- Nunca commite o JWT secret no repositório
- Use diferentes secrets para desenvolvimento e produção
- Rotacione o secret periodicamente em produção
- Mantenha o secret seguro e acessível apenas para administradores

## Rotação de Secret

Se precisar rotacionar o secret:

1. Gere um novo secret usando um dos métodos acima
2. Configure o novo secret como variável de ambiente
3. Reinicie a aplicação
4. **Nota**: Todos os tokens JWT existentes serão invalidados!

