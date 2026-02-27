# Solução para Problema de Memória na Compilação

## Problema
Erro: `There is insufficient memory for the Java Runtime Environment to continue`
`Native memory allocation (malloc) failed`

## Soluções Implementadas

### 1. Configuração JVM (`.mvn/jvm.config`) ✅ FUNCIONANDO
- Heap máximo: 512MB
- Heap inicial: 64MB
- Metaspace: 128MB
- Code Cache: 32MB (Reserved), 16MB (Initial)
- Garbage Collector: SerialGC (mais econômico)

### 2. Configuração do Maven Compiler Plugin
- Memória inicial: 128MB
- Memória máxima: 768MB
- Opções adicionais de metaspacia e code cache

## Soluções Alternativas

### Opção 1: Compilar com Script de Baixa Memória
```powershell
.\compile-low-memory.ps1
```

### Opção 2: Compilar sem Testes
```powershell
mvn clean compile -DskipTests
```

### Opção 3: Compilar Incrementalmente
```powershell
# Compilar apenas classes modificadas
mvn compile -pl . -am
```

### Opção 4: Aumentar Arquivo de Paginação do Windows

**IMPORTANTE**: Este é o problema mais provável. O erro "arquivo de paginação muito pequeno" indica que o Windows precisa de mais memória virtual.

**Passos para aumentar:**

1. Abra **Painel de Controle** → **Sistema**
2. Clique em **Configurações avançadas do sistema**
3. Na aba **Avançado**, clique em **Configurações** (em Desempenho)
4. Na aba **Avançado**, clique em **Alterar** (em Memória virtual)
5. **Desmarque** "Gerenciar automaticamente o tamanho do arquivo de paginação para todas as unidades"
6. Selecione a unidade do sistema (geralmente C:)
7. Selecione **Tamanho personalizado**
8. Configure:
   - **Tamanho inicial**: 4096 MB (ou 4x a RAM física)
   - **Tamanho máximo**: 8192 MB (ou 8x a RAM física)
9. Clique em **Definir** e **OK**
10. **Reinicie o computador**

### Opção 5: Reduzir ainda mais a memória ✅ CONFIGURAÇÃO ATUAL

A configuração atual que está funcionando (em `.mvn/jvm.config`):

```
-Xmx512m
-Xms64m
-XX:MaxMetaspaceSize=128m
-XX:+UseSerialGC
-XX:ReservedCodeCacheSize=32m
-XX:InitialCodeCacheSize=16m
```

**NOTA**: Esta configuração funcionou com sucesso! A compilação completa de 149 arquivos fonte foi concluída em ~32 segundos.

### Opção 6: Compilar sem MapStruct (temporariamente)

Comente o processador de anotações no `pom.xml`:

```xml
<annotationProcessorPaths>
    <!-- Temporariamente desabilitado por questões de memória -->
    <!--
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${org.mapstruct.version}</version>
    </path>
    -->
</annotationProcessorPaths>
```

Depois, gere os mappers manualmente ou em uma compilação separada.

## Verificar Uso de Memória

Para verificar quanto de memória está sendo usado:

```powershell
# Ver processos Java
Get-Process java | Select-Object Id, ProcessName, @{Name="Memory(MB)";Expression={[math]::Round($_.WS/1MB,2)}}

# Ver uso geral de memória
systeminfo | findstr /C:"Total Physical Memory" /C:"Available Physical Memory"
```

## Status Atual ✅

**PROBLEMA RESOLVIDO!** A compilação está funcionando com as configurações atuais:
- Heap: 512MB máximo, 64MB inicial
- Metaspace: 128MB
- Code Cache: 32MB
- **Build bem-sucedido**: 149 arquivos fonte compilados em ~32 segundos

## Recomendações Finais

1. ✅ **Solução aplicada e funcionando**: Configurações reduzidas em `.mvn/jvm.config`
2. **Para compilar normalmente**: Use `mvn clean compile -DskipTests`
3. **Se precisar compilar testes também**: Use `mvn test` (pode precisar de mais memória)
4. **Solução permanente para melhor performance**: Aumentar o arquivo de paginação do Windows (Opção 4) ou adicionar mais RAM física
