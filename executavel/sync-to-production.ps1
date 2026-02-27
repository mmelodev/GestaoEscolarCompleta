# Script de Sincronização Local → Produção (PowerShell)
# Este script ajuda a garantir que todas as mudanças sejam commitadas e enviadas

Write-Host "🔄 Iniciando sincronização..." -ForegroundColor Yellow

# Verificar se está em um repositório git
try {
    $null = git rev-parse --git-dir 2>&1
} catch {
    Write-Host "❌ Erro: Não é um repositório Git!" -ForegroundColor Red
    Write-Host "Inicialize o repositório com: git init" -ForegroundColor Yellow
    exit 1
}

# Verificar status
Write-Host "`n📋 Verificando status do repositório..." -ForegroundColor Yellow
git status

# Verificar se há mudanças
$status = git status --porcelain
if ([string]::IsNullOrWhiteSpace($status)) {
    Write-Host "`n✅ Não há mudanças para commitar" -ForegroundColor Green
} else {
    Write-Host "`n📦 Adicionando arquivos modificados..." -ForegroundColor Yellow
    git add .
    
    Write-Host "💾 Criando commit..." -ForegroundColor Yellow
    $commitMessage = @"
feat: Correções e melhorias - login, alunos, turmas e data de nascimento

- Adicionada frase em coreano na tela de login
- Corrigido erro 500 em turmas (professorResponsavel)
- Corrigido problema de data de nascimento no cadastro de aluno
- Melhorada persistência de turmas (cache e lazy loading)
- Criada página de menu de alunos com cards
- Adicionado tratamento de erros para evitar 502
- Corrigido @Cacheable no TurmaService
- Criado checklist de sincronização
"@
    
    git commit -m $commitMessage
    
    Write-Host "`n🚀 Preparando para enviar ao repositório remoto..." -ForegroundColor Yellow
    
    # Tentar detectar branch atual
    $branch = git branch --show-current
    if ([string]::IsNullOrWhiteSpace($branch)) {
        $branch = "main"
    }
    
    Write-Host "Branch atual: $branch" -ForegroundColor Cyan
    $response = Read-Host "Deseja fazer push para origin/$branch? (s/n)"
    
    if ($response -match "^[SsYy]$") {
        git push origin $branch
        if ($LASTEXITCODE -eq 0) {
            Write-Host "`n✅ Push realizado com sucesso!" -ForegroundColor Green
        } else {
            Write-Host "`n❌ Erro ao fazer push. Verifique a configuração do remote." -ForegroundColor Red
            Write-Host "Configure o remote com: git remote add origin <url>" -ForegroundColor Yellow
        }
    } else {
        Write-Host "`n⏭️  Push cancelado pelo usuário" -ForegroundColor Yellow
    }
}

Write-Host "`n✅ Sincronização concluída!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Próximos passos:" -ForegroundColor Cyan
Write-Host "1. Verifique se o deploy automático foi acionado (Railway/GitHub Actions)"
Write-Host "2. Verifique as variáveis de ambiente em produção"
Write-Host "3. Teste as funcionalidades após o deploy"
Write-Host "4. Consulte SYNC_CHECKLIST.md para mais detalhes"

