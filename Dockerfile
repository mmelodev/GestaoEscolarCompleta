# ========================================
# Estágio 1: Build da Aplicação
# ========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Define o diretório de trabalho
WORKDIR /app

# Copia apenas os arquivos de configuração do Maven (cache de dependências)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Baixa as dependências (esta camada será reutilizada se o pom.xml não mudar)
RUN mvn dependency:go-offline -B

# Copia o código fonte
COPY src ./src

# Compila e empacota a aplicação
RUN mvn clean package -DskipTests -B

# Verifica se o JAR foi gerado
RUN test -f target/*.jar || (echo "ERRO: JAR não foi gerado!" && exit 1)

# ========================================
# Estágio 2: Runtime da Aplicação
# ========================================
FROM eclipse-temurin:21-jre-jammy

# Metadados da imagem
LABEL maintainer="AriranG <contato@arirang.com.br>"
LABEL description="AriranG Plataforma - Sistema de gestão escolar para escolas de idiomas"
LABEL version="0.0.1-SNAPSHOT"

# Cria um usuário não-root para segurança
RUN groupadd -r spring && useradd -r -g spring spring

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Cria diretórios necessários e ajusta permissões
RUN mkdir -p /app/logs /app/uploads/fotos-alunos && \
    chown -R spring:spring /app

# Muda para o usuário não-root
USER spring:spring

# Expõe a porta da aplicação
EXPOSE 8080

# Define variáveis de ambiente padrão
ENV SPRING_PROFILES_ACTIVE=prod
# JAVA_OPTS: Configurar com base na memória disponível do Railway
# Railway free tier geralmente tem ~512MB-1GB de RAM total
# Para evitar OOM, usar valores MUITO conservadores
# Heap reduzido ao mínimo viável para Spring Boot
ENV JAVA_OPTS="-Xmx192m -Xms96m -XX:+UseContainerSupport -XX:MaxRAMPercentage=45.0 -XX:InitialRAMPercentage=30.0 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:MaxMetaspaceSize=128m -XX:CompressedClassSpaceSize=64m -XX:ReservedCodeCacheSize=48m -XX:+DisableExplicitGC -XX:+UseStringDeduplication"

# Health check para verificar se a aplicação está rodando
# Nota: Para usar /actuator/health, adicione spring-boot-starter-actuator ao pom.xml
# Por enquanto, verifica se o processo Java está rodando
# Alternativa: Configure health check na plataforma PaaS (Railway/Render)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD pgrep -f "java.*app.jar" > /dev/null || exit 1

# Comando para executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

