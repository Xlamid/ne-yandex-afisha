package git.xlamid.eventmanagerservice.cache.config;

import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.location.dto.GetLocationDto;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@EnableCaching
@Configuration
public class RedisConfig {
    
    private final Duration defaultTtl;
    private final Duration locationTtl;
    private final Duration eventTtl;

    @Autowired
    public RedisConfig(@Value("${cache.ttl.min.default}") long defaultTtl, 
                       @Value("${cache.ttl.min.location}") long locationTtl, 
                       @Value("${cache.ttl.min.event}") long eventTtl) {
        this.defaultTtl = Duration.ofMinutes(defaultTtl);
        this.locationTtl = Duration.ofMinutes(locationTtl);
        this.eventTtl = Duration.ofMinutes(eventTtl);
    }

    @Bean
    public RedisTemplate<String, LocationEntity> locationRedisTemplate(RedisConnectionFactory factory,
                                                                       ObjectMapper objectMapper) {
        return buildTemplate(factory, objectMapper, LocationEntity.class);
    }
    
    @Bean
    public RedisTemplate<String, EventEntity> eventRedisTemplate(RedisConnectionFactory factory,
                                                                 ObjectMapper objectMapper) {
        return buildTemplate(factory, objectMapper, EventEntity.class);
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory,
                                     ObjectMapper objectMapper) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(defaultTtl)
                .disableCachingNullValues()
                .serializeKeysWith(SerializationPair
                        .fromSerializer(new StringRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "location", buildConfig(defaultConfig, locationTtl, objectMapper, GetLocationDto.class),
                "locations", buildConfig(defaultConfig, locationTtl, objectMapper,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, GetLocationDto.class)),
                "event", buildConfig(defaultConfig, eventTtl, objectMapper, GetEventDto.class)
        );
        
        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }
    
    private <T> RedisTemplate<String, T> buildTemplate(RedisConnectionFactory factory,
                                                       ObjectMapper objectMapper,
                                                       Class<T> targetClass) {
        RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(new JacksonJsonRedisSerializer<>(objectMapper, targetClass));
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private <T> RedisCacheConfiguration buildConfig(RedisCacheConfiguration config,
                                                    Duration ttl,
                                                    ObjectMapper objectMapper,
                                                    Class<T> targetClass) {
        return config.entryTtl(ttl)
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(objectMapper, targetClass)));
    }

    private RedisCacheConfiguration buildConfig(RedisCacheConfiguration config,
                                                Duration ttl,
                                                ObjectMapper objectMapper,
                                                JavaType type) {
        return config.entryTtl(ttl)
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(objectMapper, type)));
    }
}