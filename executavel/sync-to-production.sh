#!/bin/bash

# Script de Sincronização Local → Produção
# Este script ajuda a garantir que todas as mudanças sejam commitadas e enviadas

echo "🔄 Iniciando sincronização..."

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Verificar se está em um repositório git
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo -e "${RED}❌ Erro: Não é um repositório Git!${NC}"
    echo "Inicialize o repositório com: git init"
    exit 1
fi

# Verificar status
echo -e "${YELLOW}📋 Verificando status do repositório...${NC}"
git status

# Verificar se há mudanças
if [ -z "$(git status --porcelain)" ]; then
    echo -e "${GREEN}✅ Não há mudanças para commitar${NC}"
else
    echo -e "${YELLOW}📦 Adicionando arquivos modificados...${NC}"
    git add .
    
    echo -e "${YELLOW}💾 Criando commit...${NC}"
    git commit -m "feat: Correções e melhorias - login, alunos, turmas e data de nascimento

- Adicionada frase em coreano na tela de login
- Corrigido erro 500 em turmas (professorResponsavel)
- Corrigido problema de data de nascimento no cadastro de aluno
- Melhorada persistência de turmas (cache e lazy loading)
- Criada página de menu de alunos com cards
- Adicionado tratamento de erros para evitar 502
- Corrigido @Cacheable no TurmaService
- Criado checklist de sincronização"
    
    echo -e "${YELLOW}🚀 Enviando para repositório remoto...${NC}"
    
    # Tentar detectar branch atual
    BRANCH=$(git branch --show-current)
    if [ -z "$BRANCH" ]; then
        BRANCH="main"
    fi
    
    echo -e "${YELLOW}Branch atual: ${BRANCH}${NC}"
    read -p "Deseja fazer push para origin/${BRANCH}? (s/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[SsYy]$ ]]; then
        git push origin "$BRANCH"
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Push realizado com sucesso!${NC}"
        else
            echo -e "${RED}❌ Erro ao fazer push. Verifique a configuração do remote.${NC}"
            echo "Configure o remote com: git remote add origin <url>"
        fi
    else
        echo -e "${YELLOW}⏭️  Push cancelado pelo usuário${NC}"
    fi
fi

echo -e "${GREEN}✅ Sincronização concluída!${NC}"
echo ""
echo "📝 Próximos passos:"
echo "1. Verifique se o deploy automático foi acionado (Railway/GitHub Actions)"
echo "2. Verifique as variáveis de ambiente em produção"
echo "3. Teste as funcionalidades após o deploy"
echo "4. Consulte SYNC_CHECKLIST.md para mais detalhes"

