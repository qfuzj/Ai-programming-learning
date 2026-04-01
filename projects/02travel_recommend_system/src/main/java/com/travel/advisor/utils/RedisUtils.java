package com.travel.advisor.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value, Duration duration) {
        try {
            redisTemplate.opsForValue().set(key, value, duration);
            log.debug("✅ Redis SET: key={}, value={}, duration={}", key, value, duration);
        } catch (Exception e) {
            log.error("❌ Redis SET 失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            log.debug("✅ Redis SET: key={}, value={}, timeout={}ms", key, value, timeUnit.toMillis(timeout));
        } catch (Exception e) {
            log.error("❌ Redis SET 失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            T value = (T) redisTemplate.opsForValue().get(key);
            log.debug("✅ Redis GET: key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            log.error("❌ Redis GET 失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    public Boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("✅ Redis DELETE: key={}, result={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("❌ Redis DELETE 失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    public Boolean hasKey(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            log.debug("Redis HASKEY: key={}, result={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("❌ Redis HASKEY 失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    public Boolean expire(String key, Duration duration) {
        try {
            Boolean result = redisTemplate.expire(key, duration);
            log.debug("Redis EXPIRE: key={}, duration={}, result={}", key, duration, result);
            return result;
        } catch (Exception e) {
            log.error("❌ Redis EXPIRE 失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }
}
