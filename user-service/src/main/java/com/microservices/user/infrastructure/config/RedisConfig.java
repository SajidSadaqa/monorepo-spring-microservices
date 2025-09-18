// user-service/src/main/java/.../config/RedisConfig.java
package com.microservices.user.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.cache.CacheManager;

@Configuration
public class RedisConfig {

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
    RedisConnectionFactory connectionFactory,
    ObjectMapper bootObjectMapper) {

    // copy Boot's mapper (already has many sensible settings) and ensure JavaTime is present
    ObjectMapper om = bootObjectMapper.copy()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(om);

    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(serializer);
    template.afterPropertiesSet();
    return template;
  }

  // Only if you use Spring Cache (@Cacheable, etc.). Safe to omit otherwise.
  @Bean
  public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper bootObjectMapper) {
    ObjectMapper om = bootObjectMapper.copy()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(om);

    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
      .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext
        .SerializationPair.fromSerializer(serializer));

    return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
  }
}
