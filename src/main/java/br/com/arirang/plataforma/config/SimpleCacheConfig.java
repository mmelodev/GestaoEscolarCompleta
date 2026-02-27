package br.com.arirang.plataforma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de cache simples (em memória) para quando Redis não estiver disponível
 * 
 * Esta configuração é carregada apenas quando:
 * - spring.cache.type=simple (padrão), OU
 * - Quando RedisCacheConfig não está ativo
 * 
 * Funciona como fallback quando Redis não está disponível.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "simple", matchIfMissing = true)
public class SimpleCacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCacheConfig.class);

    @Bean
    @ConditionalOnMissingBean(name = "cacheManager")
    public CacheManager cacheManager() {
        logger.info("✅ Configurando cache em memória (ConcurrentMapCacheManager)");
        logger.info("   Redis não está disponível ou foi desabilitado (spring.cache.type=simple)");
        return new ConcurrentMapCacheManager();
    }
}

