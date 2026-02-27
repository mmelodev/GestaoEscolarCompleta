# Checklist de Sincronização Local ↔ Produção

## 📋 Arquivos Modificados/Criados (Sessão Atual)

### ✅ Arquivos Criados
- `src/main/resources/templates/alunos-menu.html` - Nova página de menu de alunos

### ✅ Arquivos Modificados
1. **Templates HTML:**
   - `src/main/resources/templates/login.html` - Adicionada frase em coreano
   - `src/main/resources/templates/turmas.html` - Corrigido erro de professorResponsavel
   - `src/main/resources/templates/aluno-form.html` - Validação JavaScript para data de nascimento
   - `src/main/resources/templates/home.html` - Link do card alunos atualizado
   - `src/main/resources/templates/error.html` - Link atualizado para home

2. **Controllers Java:**
   - `src/main/java/br/com/arirang/plataforma/controller/AlunoController.java`
     - Adicionado método `menuAlunos()` para `/alunos/menu`
     - Melhorado tratamento de erro e validação de data de nascimento
   - `src/main/java/br/com/arirang/plataforma/controller/AuthWebController.java`
     - Adicionado tratamento de erro para evitar 502
   - `src/main/java/br/com/arirang/plataforma/controller/HomeController.java`
     - Adicionado tratamento de erro para primeira renderização

3. **Services Java:**
   - `src/main/java/br/com/arirang/plataforma/service/TurmaService.java`
     - Corrigido `@Cacheable` com `unless` correto
     - Melhorado `saveAndFlush()` para persistência imediata
     - Adicionado `@Transactional(readOnly = true)` na listagem

4. **Configurações:**
   - `src/main/java/br/com/arirang/plataforma/config/WebConfig.java`
     - Adicionado formatador de data ISO_LOCAL_DATE para LocalDate

## 🔄 Passos para Sincronização

### 1. Commit Local (Git)
```bash
# Verificar status
git status

# Adicionar todos os arquivos modificados
git add .

# Commit com mensagem descritiva
git commit -m "feat: Correções e melhorias - login, alunos, turmas e data de nascimento

- Adicionada frase em coreano na tela de login
- Corrigido erro 500 em turmas (professorResponsavel)
- Corrigido problema de data de nascimento no cadastro de aluno
- Melhorada persistência de turmas (cache e lazy loading)
- Criada página de menu de alunos com cards
- Adicionado tratamento de erros para evitar 502
- Corrigido @Cacheable no TurmaService"

# Push para repositório remoto
git push origin main
# ou
git push origin master
```

### 2. Verificar Variáveis de Ambiente em Produção

#### Variáveis Obrigatórias em Produção:
- `SPRING_PROFILES_ACTIVE=prod`
- `JWT_SECRET` (mínimo 32 caracteres)
- `DB_URL` ou `MYSQL_URL` (Railway)
- `DB_USERNAME`
- `DB_PASSWORD`
- `CORS_ALLOWED_ORIGIN_PATTERNS` (se necessário)
- `CORS_ALLOWED_ORIGINS` (se necessário)

#### Variáveis Opcionais:
- `CACHE_TYPE=simple` (padrão)
- `CACHE_ENABLED=true`
- `CACHE_DEFAULT_TTL=PT5M`
- `UPLOAD_MAX_FILE_SIZE=5242880`
- `APP_DEFAULT_ADMIN_PASSWORD` (deixar vazio em produção)

### 3. Deploy em Produção

#### Railway:
- O deploy automático deve ocorrer após o push
- Verificar logs em caso de erro
- Verificar se as variáveis de ambiente estão configuradas

#### Manual (se necessário):
```bash
# Build do projeto
mvn clean package -DskipTests

# Deploy manual (ajustar conforme sua plataforma)
```

### 4. Verificações Pós-Deploy

- [ ] Tela de login carrega sem erro 502
- [ ] Frase em coreano aparece na tela de login
- [ ] Página `/turmas` carrega sem erro 500
- [ ] Cadastro de aluno aceita data de nascimento corretamente
- [ ] Turmas são salvas e listadas com todos os campos
- [ ] Menu de alunos (`/alunos/menu`) funciona corretamente
- [ ] Cards na home redirecionam corretamente
- [ ] Dashboard carrega na primeira renderização

## 🔍 Verificações de Configuração

### application.properties (Base)
- ✅ `spring.profiles.active=dev` (local)
- ✅ `spring.mvc.hiddenmethod.filter.enabled=true`
- ✅ Thymeleaf configurado

### application-dev.properties (Desenvolvimento)
- ✅ Database local configurado
- ✅ `spring.jpa.show-sql=true`
- ✅ `spring.jpa.hibernate.ddl-auto=update`
- ✅ Swagger habilitado

### application-prod.properties (Produção)
- ✅ Database com suporte Railway
- ✅ `spring.jpa.show-sql=false`
- ✅ `spring.jpa.hibernate.ddl-auto=update` (ou `validate` após primeira execução)
- ✅ Swagger desabilitado
- ✅ Cache de Thymeleaf habilitado
- ✅ CORS configurado para domínio de produção

## 📝 Notas Importantes

1. **Data de Nascimento**: O formato ISO (yyyy-MM-dd) é aceito automaticamente pelo Spring Boot quando configurado no `WebConfig`.

2. **Cache de Turmas**: O cache foi ajustado para evitar problemas de lazy loading. Em produção, considere usar Redis se necessário.

3. **Erro 502**: O tratamento de erro foi adicionado, mas se persistir, verifique:
   - Timeout do servidor
   - Inicialização lenta do banco de dados
   - Recursos insuficientes (memória/CPU)

4. **Professor Responsável**: A funcionalidade não está implementada na entidade Turma. O template foi ajustado para mostrar "Não atribuído".

## 🚀 Comandos Úteis

### Verificar diferenças entre branches
```bash
git diff main origin/main
```

### Verificar status do repositório
```bash
git status
git log --oneline -10
```

### Testar localmente antes de fazer push
```bash
mvn clean install
mvn spring-boot:run
```

## ⚠️ Avisos

- **NUNCA** commite arquivos com senhas ou secrets
- **SEMPRE** verifique o `.gitignore` antes de commitar
- **SEMPRE** teste localmente antes de fazer deploy em produção
- **MANTENHA** as variáveis de ambiente seguras e não versionadas

