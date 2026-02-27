package br.com.arirang.plataforma.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Duration;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
@ConditionalOnExpression("${app.cache.enabled:true}")
public class RedisCacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheConfig.class);

    private final Duration defaultTtl;
    private final boolean cacheNullValues;
    private final String keyPrefix;
    private final boolean cacheEnabled;

    public RedisCacheConfig(
            @Value("${app.cache.default-ttl:PT10M}") Duration defaultTtl,
            @Value("${app.cache.cache-null-values:false}") boolean cacheNullValues,
            @Value("${app.cache.key-prefix:plataforma::}") String keyPrefix,
            @Value("${app.cache.enabled:true}") boolean cacheEnabled) {
        this.defaultTtl = defaultTtl;
        this.cacheNullValues = cacheNullValues;
        this.keyPrefix = keyPrefix;
        this.cacheEnabled = cacheEnabled;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        if (!cacheEnabled) {
            logger.warn("Cache Redis desabilitado via propriedade. Utilizando cache em memória (ConcurrentMapCacheManager).");
            return fallbackCacheManager();
        }

        RedisCacheConfiguration cacheConfiguration = redisCacheConfiguration();
        try {
            return RedisCacheManager.builder(redisConnectionFactory)
                    .cacheDefaults(cacheConfiguration)
                    .transactionAware()
                    .build();
        } catch (Exception ex) {
            logger.error("Não foi possível inicializar o Redis Cache Manager. Utilizando fallback em memória. Motivo: {}", ex.getMessage());
            logger.debug("Detalhes da falha ao inicializar Redis Cache Manager", ex);
            return fallbackCacheManager();
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper());

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(valueSerializer);
        try {
            template.afterPropertiesSet();
        } catch (Exception ex) {
            logger.warn("Não foi possível inicializar RedisTemplate. Alguns recursos de cache podem não estar disponíveis: {}", ex.getMessage());
            logger.debug("Detalhes da falha ao inicializar RedisTemplate", ex);
        }
        return template;
    }

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(@NonNull RuntimeException exception,
                                           @Nullable Cache cache,
                                           @NonNull Object key) {
                logger.warn("Falha ao ler do cache {} com chave {}: {}", cache != null ? cache.getName() : "<desconhecido>", key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(@NonNull RuntimeException exception,
                                            @Nullable Cache cache,
                                            @NonNull Object key,
                                            @Nullable Object value) {
                logger.warn("Falha ao escrever no cache {} com chave {}: {}", cache != null ? cache.getName() : "<desconhecido>", key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(@NonNull RuntimeException exception,
                                              @Nullable Cache cache,
                                              @NonNull Object key) {
                logger.warn("Falha ao limpar cache {} para chave {}: {}", cache != null ? cache.getName() : "<desconhecido>", key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(@NonNull RuntimeException exception,
                                              @Nullable Cache cache) {
                logger.warn("Falha ao limpar cache {}: {}", cache != null ? cache.getName() : "<desconhecido>", exception.getMessage());
            }
        };
    }

    private RedisCacheConfiguration redisCacheConfiguration() {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper());

        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(defaultTtl)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .prefixCacheNameWith(keyPrefix);

        if (!cacheNullValues) {
            configuration = configuration.disableCachingNullValues();
        }

        return configuration;
    }

    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    private CacheManager fallbackCacheManager() {
        return new org.springframework.cache.concurrent.ConcurrentMapCacheManager();
    }
}


