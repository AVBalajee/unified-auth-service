package com.example.unifiedauthservice.service;

import com.example.unifiedauthservice.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    public boolean allowLoginAttempt(String tenantCode, String username, String ip) {
        return allow("rl:login:" + tenantCode + ":" + username + ":" + ip,
                properties.getLoginMaxAttempts(), properties.getLoginWindowSeconds());
    }

    public boolean allowTokenAttempt(String tenantCode, String clientOrUser, String ip) {
        return allow("rl:token:" + tenantCode + ":" + clientOrUser + ":" + ip,
                properties.getTokenMaxAttempts(), properties.getTokenWindowSeconds());
    }

    private boolean allow(String key, int maxAttempts, int windowSeconds) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            return count == null || count <= maxAttempts;
        } catch (Exception ex) {
            return true;
        }
    }
}
