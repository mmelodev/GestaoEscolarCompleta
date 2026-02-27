package br.com.arirang.plataforma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing)
 * 
 * Esta configuração permite que aplicações frontend (React, Vue, Angular, etc.)
 * façam requisições para a API REST deste backend.
 * 
 * IMPORTANTE:
 * - Em desenvolvimento: permite localhost com qualquer porta
 * - Em produção: permite apenas origens específicas configuradas via propriedades
 */
@Configuration
public class CorsConfig {

    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-origin-patterns:}")
    private String allowedOriginPatterns;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Configuração baseada no perfil (dev ou prod)
        if ("dev".equals(activeProfile)) {
            configureDevelopmentCors(configuration);
        } else {
            configureProductionCors(configuration);
        }
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));
        
        // Headers permitidos na requisição
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "X-XSRF-TOKEN",
            "XSRF-TOKEN",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Headers expostos na resposta
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "XSRF-TOKEN",
            "Content-Disposition",
            "Content-Length"
        ));
        
        // Permitir credenciais (cookies, authorization headers, etc.)
        configuration.setAllowCredentials(true);
        
        // Tempo de cache para preflight requests (1 hora)
        configuration.setMaxAge(3600L);
        
        // Aplicar configuração CORS para todas as rotas da API
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }

    /**
     * Configuração CORS para desenvolvimento
     * Permite localhost com qualquer porta e 127.0.0.1
     */
    private void configureDevelopmentCors(CorsConfiguration configuration) {
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://[::1]:*"
        ));
    }

    /**
     * Configuração CORS para produção
     * Permite apenas origens específicas configuradas via propriedades
     */
    private void configureProductionCors(CorsConfiguration configuration) {
        // Se houver padrões de origem configurados, use-os
        if (allowedOriginPatterns != null && !allowedOriginPatterns.trim().isEmpty()) {
            List<String> patterns = Arrays.stream(allowedOriginPatterns.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            if (!patterns.isEmpty()) {
                configuration.setAllowedOriginPatterns(patterns);
                return;
            }
        }
        // Se houver origens específicas configuradas, use-as
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            List<String> origins = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            if (!origins.isEmpty()) {
                configuration.setAllowedOrigins(origins);
                return;
            }
        }
        // Se nada estiver configurado, negar tudo (mais seguro)
        // Em vez de lançar exceção, apenas logamos um aviso e negamos todas as origens
        configuration.setAllowedOriginPatterns(List.of());
        logger.warn(
            "CORS não configurado para produção. Todas as requisições cross-origin serão bloqueadas. " +
            "Configure 'app.cors.allowed-origins' ou 'app.cors.allowed-origin-patterns' " +
            "no arquivo application-prod.properties ou via variáveis de ambiente."
        );
    }
}
