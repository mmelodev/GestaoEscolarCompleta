# 📤 Como Enviar Templates do Cliente para Adaptação

## 🎯 Objetivo

Este guia explica como você pode me enviar os templates que o cliente forneceu para que eu possa adaptá-los para Thymeleaf e integrá-los ao sistema.

---

## 📋 OPÇÕES DE ENVIO

### Opção 1: Colar Conteúdo Diretamente (Recomendado)

**Para templates em formato:**
- Word (.doc, .docx)
- PDF
- HTML
- Texto simples

**Como fazer:**
1. Abra o arquivo do template
2. Selecione todo o conteúdo (Ctrl+A)
3. Copie (Ctrl+C)
4. Cole aqui na conversa
5. Identifique qual template é (ex: "Template 1: Contrato Serviços Menor")

**Exemplo:**
```
Template 1: Contrato de Prestação de Serviços - Menor de Idade

[COLAR AQUI O CONTEÚDO COMPLETO DO TEMPLATE]

Inclui:
- Texto fixo
- Campos variáveis
- Formatação desejada
```

---

### Opção 2: Descrever a Estrutura

Se você não conseguir copiar o conteúdo, descreva:

**Template: [Nome]**

```markdown
## Campos que devem aparecer:
- [Campo 1]: [Descrição]
- [Campo 2]: [Descrição]
- etc.

## Layout:
- [Descrição do layout]
- [Posicionamento dos elementos]
- [Cores, fontes, etc.]

## Texto Fixo:
[Colar aqui todo o texto que não muda]

## Cláusulas:
[Colar aqui as cláusulas do contrato]
```

---

### Opção 3: Enviar Imagens/Screenshots

Se os templates estiverem em PDF ou imagem:

1. Tire screenshots de cada seção
2. Descreva o que cada seção contém
3. Identifique campos variáveis vs. texto fixo

---

## 📝 FORMATO IDEAL PARA ENVIO

### Estrutura Recomendada

Para cada template, envie:

```markdown
## TEMPLATE: [Nome do Template]

### Tipo:
- [ ] Contrato de Serviços
- [ ] Autorização de Uso de Imagem
- [ ] Outro: ___________

### Público-Alvo:
- [ ] Menor de Idade
- [ ] Maior de Idade
- [ ] Ambos

### Campos Variáveis Necessários:
1. **Aluno:**
   - Nome completo
   - CPF
   - Data de nascimento
   - [outros campos]

2. **Responsável (se menor):**
   - Nome completo
   - CPF
   - RG
   - Telefone
   - E-mail
   - [outros campos]

3. **Contrato:**
   - Número do contrato
   - Data do contrato
   - Período de vigência
   - Turma
   - Valores
   - [outros campos]

### Texto Fixo:
[COLAR TODO O TEXTO QUE NÃO MUDA]

### Formatação Especial:
- [ ] Cabeçalho com logo
- [ ] Rodapé com informações
- [ ] Assinaturas
- [ ] Carimbo/Data
- [ ] Outros: ___________

### Observações:
[Qualquer observação importante sobre o template]
```

---

## 🔍 EXEMPLO PRÁTICO

### Exemplo 1: Template Simples

```
TEMPLATE: Contrato de Serviços - Menor de Idade

Tipo: Contrato de Prestação de Serviços
Público-Alvo: Menor de Idade

Campos Variáveis:
- Nome do responsável
- CPF do responsável
- Nome do aluno
- Data nascimento do aluno
- Turma
- Valor total
- Período de vigência

Texto Fixo:
CONTRATO DE PRESTAÇÃO DE SERVIÇOS EDUCACIONAIS
MENOR DE IDADE

Pelo presente instrumento particular de contrato de prestação de serviços 
educacionais, de um lado como CONTRATANTE, [NOME DO RESPONSÁVEL], 
CPF [CPF], e de outro lado como CONTRATADO, AriranG Escola de Idiomas...

[CONTINUAR COM TODO O TEXTO]
```

### Exemplo 2: Template com Estrutura Complexa

```
TEMPLATE: Autorização de Uso de Imagem - Menor

Tipo: Autorização
Público-Alvo: Menor de Idade

Estrutura:
1. Cabeçalho: "AUTORIZAÇÃO DE USO DE IMAGEM"
2. Dados do responsável
3. Dados do aluno
4. Texto de autorização
5. Assinatura

Campos:
- Nome responsável
- CPF responsável
- Nome aluno
- Data nascimento aluno

Texto:
Eu, [NOME DO RESPONSÁVEL], CPF [CPF], responsável legal pelo menor 
[NOME DO ALUNO], nascido em [DATA], autorizo...

[CONTINUAR]
```

---

## ✅ CHECKLIST ANTES DE ENVIAR

Antes de enviar os templates, verifique:

- [ ] Identifiquei qual é cada template (nome claro)
- [ ] Copiei todo o conteúdo ou descrevi completamente
- [ ] Identifiquei campos variáveis vs. texto fixo
- [ ] Incluí informações sobre formatação especial
- [ ] Incluí cláusulas e textos legais completos
- [ ] Especifiquei se é para menor ou maior de idade

---

## 🎨 INFORMAÇÕES ADICIONAIS ÚTEIS

### Se o Template tiver Logo/Imagens

Descreva:
- Onde a logo deve aparecer (cabeçalho, rodapé, etc.)
- Tamanho aproximado
- Se deve ser centralizada, à esquerda, etc.

### Se o Template tiver Tabelas

Descreva:
- Quantas colunas
- O que cada coluna contém
- Se há linhas fixas ou dinâmicas

### Se o Template tiver Assinaturas

Descreva:
- Quantas assinaturas
- Onde devem aparecer
- Se há campos de data/hora
- Se há espaço para carimbo

---

## 🚀 APÓS ENVIAR

Depois que você enviar os templates:

1. ✅ Eu vou adaptar cada template para Thymeleaf
2. ✅ Vou criar os arquivos HTML em `templates/contratos/pdf/`
3. ✅ Vou integrar com o controller
4. ✅ Vou testar a geração de PDFs
5. ✅ Vou fornecer instruções de ajustes finais (se necessário)

---

## 💡 DICAS

1. **Seja específico:** Quanto mais detalhes, melhor será a adaptação
2. **Inclua tudo:** Mesmo texto que parece óbvio, inclua
3. **Formatação:** Descreva cores, fontes, tamanhos se forem importantes
4. **Ordem:** Mantenha a ordem dos elementos como no original
5. **Exemplos:** Se possível, inclua exemplos de como os campos devem aparecer preenchidos

---

## 📞 PRONTO PARA ENVIAR?

Quando estiver pronto, simplesmente:

1. Cole o conteúdo dos templates aqui na conversa
2. Identifique cada template claramente
3. Eu faço o resto! 🎉

---

**Última atualização:** Dezembro 2024

