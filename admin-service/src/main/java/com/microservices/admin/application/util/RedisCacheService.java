package com.microservices.admin.application.util;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility service for interacting with Redis.
 * Provides TTL-aware methods for simple values, lists, maps, and generic objects.
 */
@Service
@AllArgsConstructor
public class RedisCacheService {

  private final RedisTemplate<String, Object> redisTemplate;

  public <T> void putValue(String key, T value, long ttlSeconds) {
    if (ttlSeconds > 0) {
      redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
    } else {
      redisTemplate.opsForValue().set(key, value);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(String key, Class<T> type) {
    Object val = redisTemplate.opsForValue().get(key);
    return val == null ? null : (T) val;
  }

  public <T> void putList(String key, List<T> list, long ttlSeconds) {
    ListOperations<String, Object> ops = redisTemplate.opsForList();
    if (list != null && !list.isEmpty()) {
      ops.rightPushAll(key, new ArrayList<>(list));
    }
    if (ttlSeconds > 0) {
      redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
    }
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getList(String key, Class<T> type) {
    List<Object> raw = redisTemplate.opsForList().range(key, 0, -1);
    List<T> result = new ArrayList<>();
    if (raw != null) {
      for (Object obj : raw) {
        result.add(obj == null ? null : (T) obj);
      }
    }
    return result;
  }

  public <K, V> void putMap(String key, Map<K, V> map, long ttlSeconds) {
    if (map != null && !map.isEmpty()) {
      redisTemplate.opsForHash().putAll(key, map);
    }
    if (ttlSeconds > 0) {
      redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
    }
  }

  @SuppressWarnings("unchecked")
  public <V> Map<Object, V> getMap(String key, Class<V> valueType) {
    Map<Object, Object> raw = redisTemplate.opsForHash().entries(key);
    Map<Object, V> result = new HashMap<>();
    for (Map.Entry<Object, Object> entry : raw.entrySet()) {
      result.put(entry.getKey(), entry.getValue() == null ? null : (V) entry.getValue());
    }
    return result;
  }

  public <T> void putObject(String key, T value, long ttlSeconds) {
    putValue(key, value, ttlSeconds);
  }

  public <T> T getObject(String key, Class<T> type) {
    return getValue(key, type);
  }
}
