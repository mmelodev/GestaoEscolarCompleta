package br.com.arirang.plataforma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuração especial para Railway que converte MYSQL_URL 
 * (formato mysql://user:pass@host:port/db) para formato JDBC
 * 
 * Esta classe intercepta a MYSQL_URL do Railway e converte para o formato
 * JDBC antes do Spring Boot inicializar o DataSource.
 */
@Configuration
@Profile("prod")
public class RailwayDatabaseConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RailwayDatabaseConfig.class);

    @Override
    public void onApplicationEvent(@org.springframework.lang.NonNull ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();
        
        // Verificar se estamos em produção antes de aplicar configurações do Railway
        String activeProfile = env.getProperty("spring.profiles.active", "dev");
        boolean isProduction = "prod".equals(activeProfile);
        
        // Apenas aplicar configurações do Railway em produção ou se MYSQL_URL estiver presente
        String mysqlUrl = System.getenv("MYSQL_URL");
        
        // Se estiver em dev e não houver MYSQL_URL, não interferir nas configurações locais
        if (!isProduction && (mysqlUrl == null || mysqlUrl.isEmpty())) {
            logger.debug("RailwayDatabaseConfig: Modo desenvolvimento sem MYSQL_URL. Não aplicando configurações do Railway.");
            return;
        }
        
        // SEMPRE definir propriedades padrão primeiro para evitar erros de placeholder
        // Isso previne erros se alguma variável não existir
        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("spring.datasource.url", 
            "jdbc:mysql://localhost:3306/railway?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo");
        defaultProperties.put("spring.datasource.username", "root");
        defaultProperties.put("spring.datasource.password", "");
        env.getPropertySources().addFirst(
            new MapPropertySource("railwayDbConfigDefaults", defaultProperties)
        );
        
        // Verificar todas as variáveis relacionadas ao MySQL para diagnóstico
        // Usar System.getenv() diretamente para evitar referência circular de placeholders
        // (mysqlUrl já foi lido acima, mas vamos garantir)
        if (mysqlUrl == null) {
            mysqlUrl = System.getenv("MYSQL_URL");
        }
        String dbUrl = System.getenv("DB_URL");
        String dbHost = System.getenv("DB_HOST");
        
        // Se MYSQL_URL contém placeholder não resolvido (${MYSQL_URL} ou ${{MYSQL_URL}}), tentar resolver via env
        if (mysqlUrl != null && (mysqlUrl.startsWith("${") || mysqlUrl.startsWith("${{"))) {
            logger.warn("MYSQL_URL contém placeholder não resolvido: {}. Tentando resolver via env.getProperty()...", mysqlUrl);
            try {
                // Tentar resolver via environment, mas capturar exceção se houver referência circular
                String resolved = env.getProperty("MYSQL_URL");
                if (resolved != null && !resolved.equals(mysqlUrl) && !resolved.startsWith("${")) {
                    mysqlUrl = resolved;
                    logger.info("MYSQL_URL resolvida via env.getProperty(): {}", 
                        mysqlUrl.contains("@") ? mysqlUrl.substring(0, mysqlUrl.indexOf("@")) + "@****" : mysqlUrl);
                } else {
                    logger.error("MYSQL_URL não pôde ser resolvida. Placeholder: {}", mysqlUrl);
                    mysqlUrl = null; // Marcar como inválida para usar valores padrão
                }
            } catch (IllegalArgumentException e) {
                logger.error("Erro ao resolver MYSQL_URL via env.getProperty(): {}", e.getMessage());
                mysqlUrl = null; // Marcar como inválida para usar valores padrão
            }
        }
        
        logger.info("=".repeat(80));
        logger.info("=== RailwayDatabaseConfig: Configuração de Database ===");
        logger.info("Perfil ativo: {}", activeProfile);
        logger.info("MYSQL_URL presente: {}", mysqlUrl != null && !mysqlUrl.isEmpty() ? "SIM" : "NÃO");
        if (mysqlUrl != null) {
            // Logar MYSQL_URL (sem senha) para diagnóstico
            String mysqlUrlSafe = mysqlUrl.contains("@") 
                ? mysqlUrl.substring(0, mysqlUrl.indexOf("@")) + "@****" 
                : (mysqlUrl.startsWith("${") ? mysqlUrl : mysqlUrl.substring(0, Math.min(50, mysqlUrl.length())) + "...");
            logger.info("MYSQL_URL valor: {}", mysqlUrlSafe);
        }
        logger.info("DB_URL presente: {}", dbUrl != null && !dbUrl.isEmpty() ? "SIM" : "NÃO");
        logger.info("DB_HOST presente: {}", dbHost != null && !dbHost.isEmpty() ? "SIM" : "NÃO");
        logger.info("DB_USERNAME presente: {}", System.getenv("DB_USERNAME") != null ? "SIM" : "NÃO");
        logger.info("DB_PASSWORD presente: {}", System.getenv("DB_PASSWORD") != null ? "SIM" : "NÃO");
        logger.info("=".repeat(80));
        
        // Se não há MYSQL_URL válida, as propriedades padrão já foram definidas acima
        if (mysqlUrl == null || mysqlUrl.isEmpty() || mysqlUrl.startsWith("${") || mysqlUrl.startsWith("${{")) {
            logger.error("=".repeat(80));
            logger.error("⚠️ MYSQL_URL não encontrada ou não resolvida!");
            logger.error("=".repeat(80));
            logger.error("A aplicação não conseguirá conectar ao banco de dados.");
            logger.error("");
            logger.error("SOLUÇÕES:");
            logger.error("1. Conecte o serviço MySQL ao serviço da aplicação no Railway");
            logger.error("2. Ou configure MYSQL_URL manualmente no Railway com o valor completo");
            logger.error("3. Ou configure DB_URL com a URL completa do MySQL (não use localhost!)");
            logger.error("");
            String finalUrl = env.getProperty("spring.datasource.url");
            logger.warn("URL que será usada: {}", finalUrl != null ? finalUrl.replaceAll(":[^:@]*@", ":****@") : "NÃO DEFINIDA");
            if (finalUrl != null && finalUrl.contains("localhost")) {
                logger.error("❌ ERRO: URL contém 'localhost' - isso NÃO funcionará no Railway!");
                logger.error("   No Railway, o MySQL está em outro serviço, não em localhost.");
            }
            logger.error("=".repeat(80));
            // Continuar mesmo sem MYSQL_URL - deixar o Spring tentar conectar
            // Mas os valores padrão podem não funcionar no Railway
            return;
        }
        
        // Se a URL já está no formato JDBC, não precisa converter
        if (mysqlUrl.startsWith("jdbc:mysql://")) {
            logger.info("MYSQL_URL já está no formato JDBC. Usando diretamente.");
            try {
                // Mesmo assim, adicionar ao ambiente para garantir
                Map<String, Object> properties = new HashMap<>();
                properties.put("spring.datasource.url", mysqlUrl);
                // Tentar extrair username e password da URL se estiverem presentes
                URI uri = new URI(mysqlUrl);
                if (uri.getUserInfo() != null) {
                    String[] credentials = uri.getUserInfo().split(":");
                    properties.put("spring.datasource.username", credentials.length > 0 ? credentials[0] : "root");
                    properties.put("spring.datasource.password", credentials.length > 1 ? credentials[1] : "");
                }
                env.getPropertySources().addFirst(
                    new MapPropertySource("railwayDbConfig", properties)
                );
                logger.info("URL final de conexão: {}", mysqlUrl.replaceAll(":[^:@]*@", ":****@"));
            } catch (Exception e) {
                logger.warn("Erro ao processar MYSQL_URL no formato JDBC: {}", e.getMessage());
            }
            return;
        }
        
        // Converter mysql:// para jdbc:mysql://
        if (mysqlUrl.startsWith("mysql://")) {
            try {
                logger.info("Detectada MYSQL_URL do Railway. Convertendo para formato JDBC...");
                
                // Converter URL
                ConvertedUrl converted = convertRailwayUrlToJdbc(mysqlUrl);
                
                // Adicionar propriedades ao ambiente
                Map<String, Object> properties = new HashMap<>();
                properties.put("spring.datasource.url", converted.url);
                properties.put("spring.datasource.username", converted.username);
                properties.put("spring.datasource.password", converted.password != null ? converted.password : "");
                
                env.getPropertySources().addFirst(
                    new MapPropertySource("railwayDbConfig", properties)
                );
                
                logger.info("MYSQL_URL convertida com sucesso!");
                logger.info("URL final: {}", converted.url);
                logger.info("Username: {}", converted.username);
                logger.info("Password: {}", converted.password != null && !converted.password.isEmpty() ? "****" : "(vazia)");
            } catch (Exception e) {
                logger.error("Erro ao converter MYSQL_URL do Railway: {}", e.getMessage(), e);
                // Em caso de erro, definir valores padrão para evitar falha na inicialização
                Map<String, Object> fallbackProperties = new HashMap<>();
                fallbackProperties.put("spring.datasource.password", "");
                env.getPropertySources().addFirst(
                    new MapPropertySource("railwayDbConfigFallback", fallbackProperties)
                );
            }
        } else {
            // Se chegamos aqui, mysqlUrl não é null e não começa com jdbc:mysql:// nem mysql://
            if (mysqlUrl != null) {
                String mysqlUrlSafe = mysqlUrl.contains("@") 
                    ? mysqlUrl.substring(0, mysqlUrl.indexOf("@")) + "@****" 
                    : mysqlUrl;
                logger.warn("MYSQL_URL em formato desconhecido: {}", mysqlUrlSafe);
            } else {
                logger.warn("MYSQL_URL é null em branch inesperado");
            }
        }
    }
    
    /**
     * Converte URL do Railway (mysql://user:pass@host:port/db) 
     * para formato JDBC (jdbc:mysql://host:port/db) e extrai credenciais
     */
    private ConvertedUrl convertRailwayUrlToJdbc(String railwayUrl) throws Exception {
        // Railway URL: mysql://user:password@host:port/database
        URI uri = new URI(railwayUrl);
        
        String userInfo = uri.getUserInfo();
        String[] credentials = userInfo != null ? userInfo.split(":") : new String[]{"root", ""};
        String username = credentials.length > 0 ? credentials[0] : "root";
        String password = credentials.length > 1 ? credentials[1] : "";
        
        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 3306;
        String database = uri.getPath() != null && uri.getPath().length() > 1 
            ? uri.getPath().substring(1) // Remove a barra inicial
            : "railway";
        
            // Construir URL JDBC (sem credenciais na URL, usar variáveis separadas)
            // Parâmetros adicionais para lidar com Railway "sleep" mode:
            // - autoReconnect=true: reconecta automaticamente se conexão for perdida
            // - failOverReadOnly=false: permite escrita após reconexão
            // - maxReconnects=3: tenta reconectar até 3 vezes
            // - initialTimeout=2: espera 2 segundos entre tentativas
            String jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo&autoReconnect=true&failOverReadOnly=false&maxReconnects=3&initialTimeout=2",
                host, port, database
            );
        
        return new ConvertedUrl(jdbcUrl, username, password);
    }
    
    private static class ConvertedUrl {
        final String url;
        final String username;
        final String password;
        
        ConvertedUrl(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }
    }
}

