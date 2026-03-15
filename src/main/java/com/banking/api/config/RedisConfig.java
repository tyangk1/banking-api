package com.banking.api.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Redis cache configuration with:
 * - Custom TTL per cache name
 * - JSON serialization (human-readable in Redis)
 * - Error-resilient: cache failures fallback to DB instead of throwing 500
 * - Custom key generator based on class + method + params
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisConfig implements CachingConfigurer {

    // ============ Cache Names ============
    public static final String CACHE_ACCOUNTS = "accounts";
    public static final String CACHE_ACCOUNTS_BY_USER = "accountsByUser";
    public static final String CACHE_ACCOUNT_BY_NUMBER = "accountByNumber";
    public static final String CACHE_USER_PROFILE = "userProfile";
    public static final String CACHE_TRANSACTIONS = "transactions";

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper()));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper()));
        template.afterPropertiesSet();
        log.info("Redis template configured with JSON serialization");
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(objectMapper())))
                .entryTtl(Duration.ofMinutes(10))   // Default TTL: 10 minutes
                .disableCachingNullValues();

        // Per-cache TTL configuration
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(CACHE_ACCOUNTS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put(CACHE_ACCOUNTS_BY_USER, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put(CACHE_ACCOUNT_BY_NUMBER, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put(CACHE_USER_PROFILE, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put(CACHE_TRANSACTIONS, defaultConfig.entryTtl(Duration.ofMinutes(2)));

        log.info("Redis CacheManager configured with {} cache definitions", cacheConfigs.size());

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }

    /**
     * Error-resilient cache handler: log errors and fallback to DB
     * instead of throwing 500 errors to the client.
     */
    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache GET error [{}:{}] — fallback to DB: {}", cache.getName(), key, e.getMessage());
                // Evict the corrupted entry so next request re-caches fresh data
                try { cache.evict(key); } catch (Exception ignored) {}
            }
            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.warn("Cache PUT error [{}:{}]: {}", cache.getName(), key, e.getMessage());
            }
            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache EVICT error [{}:{}]: {}", cache.getName(), key, e.getMessage());
            }
            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.warn("Cache CLEAR error [{}]: {}", cache.getName(), e.getMessage());
            }
        };
    }

    /**
     * Custom key generator: className::methodName::param1,param2,...
     */
    @Bean("customKeyGenerator")
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringJoiner joiner = new StringJoiner(",");
            for (Object param : params) {
                joiner.add(param == null ? "null" : param.toString());
            }
            return target.getClass().getSimpleName() + "::" + method.getName() + "::" + joiner;
        };
    }

    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return mapper;
    }
}
