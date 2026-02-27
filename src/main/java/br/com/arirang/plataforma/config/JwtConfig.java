package br.com.arirang.plataforma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

/**
 * Configuração e validação do JWT Secret
 * 
 * Garante que o JWT secret atende aos requisitos mínimos de segurança:
 * - Tamanho mínimo de 32 caracteres (256 bits) para HMAC-SHA256
 * - Secret obrigatório em produção
 * - Validação na inicialização da aplicação
 */
@Configuration
public class JwtConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);
    
    // Tamanho mínimo recomendado para HMAC-SHA256 (256 bits = 32 bytes)
    private static final int MIN_SECRET_LENGTH = 32;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    @Value("${jwt.secret:}")
    private String jwtSecret;
    
    @Value("${jwt.secret.min-length:32}")
    private int minSecretLength;
    
    @PostConstruct
    public void validateJwtSecret() {
        boolean isProduction = "prod".equals(activeProfile);
        
        // Em produção, o secret é obrigatório
        if (isProduction && (!StringUtils.hasText(jwtSecret) || jwtSecret.trim().isEmpty())) {
            String errorMessage = 
                "JWT_SECRET não configurado para produção! " +
                "Configure a variável de ambiente JWT_SECRET com um valor seguro " +
                "(mínimo " + MIN_SECRET_LENGTH + " caracteres).";
            logger.error("=".repeat(80));
            logger.error(errorMessage);
            logger.error("=".repeat(80));
            throw new IllegalStateException(errorMessage);
        }
        
        // Em desenvolvimento, apenas avisar se não estiver configurado
        if (!isProduction && (!StringUtils.hasText(jwtSecret) || jwtSecret.trim().isEmpty())) {
            logger.warn("=".repeat(80));
            logger.warn("JWT_SECRET não configurado! Usando valor vazio (não recomendado).");
            logger.warn("Configure a variável de ambiente JWT_SECRET para segurança adequada.");
            logger.warn("=".repeat(80));
            return;
        }
        
        // Validar tamanho mínimo
        String trimmedSecret = jwtSecret.trim();
        if (trimmedSecret.length() < MIN_SECRET_LENGTH) {
            String errorMessage = String.format(
                "JWT_SECRET muito curto! Mínimo recomendado: %d caracteres (atual: %d). " +
                "Para HMAC-SHA256, recomenda-se pelo menos 256 bits (32 caracteres).",
                MIN_SECRET_LENGTH,
                trimmedSecret.length()
            );
            
            if (isProduction) {
                logger.error("=".repeat(80));
                logger.error(errorMessage);
                logger.error("=".repeat(80));
                throw new IllegalStateException(errorMessage);
            } else {
                logger.warn("=".repeat(80));
                logger.warn(errorMessage);
                logger.warn("Em produção, isso causará falha na inicialização.");
                logger.warn("=".repeat(80));
            }
        }
        
        // Validar complexidade (em produção)
        if (isProduction && !isSecretComplexEnough(trimmedSecret)) {
            logger.warn("JWT_SECRET pode não ser suficientemente complexo. " +
                       "Recomenda-se usar uma combinação de letras, números e caracteres especiais.");
        }
        
        logger.info("JWT Secret configurado com sucesso (tamanho: {} caracteres)", trimmedSecret.length());
    }
    
    /**
     * Verifica se o secret tem complexidade suficiente
     */
    private boolean isSecretComplexEnough(String secret) {
        boolean hasLetter = secret.matches(".*[a-zA-Z].*");
        boolean hasNumber = secret.matches(".*[0-9].*");
        boolean hasSpecial = secret.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*");
        
        // Retorna true se tiver pelo menos 2 dos 3 tipos
        int types = 0;
        if (hasLetter) types++;
        if (hasNumber) types++;
        if (hasSpecial) types++;
        
        return types >= 2;
    }
}

