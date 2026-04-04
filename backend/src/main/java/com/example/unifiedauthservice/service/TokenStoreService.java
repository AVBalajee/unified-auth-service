package com.example.unifiedauthservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenStoreService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void blacklistAccessToken(String tokenId, Duration ttl) {
        try {
            redisTemplate.opsForValue().set("blacklist:" + tokenId, true, ttl);
        } catch (Exception ignored) {
        }
    }

    public boolean isBlacklisted(String tokenId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + tokenId));
        } catch (Exception ex) {
            return false;
        }
    }

    public void cacheRefreshToken(String tokenId, String tokenHash, Duration ttl) {
        try {
            redisTemplate.opsForValue().set("refresh:" + tokenId, tokenHash, ttl);
        } catch (Exception ignored) {
        }
    }

    public String getRefreshTokenHash(String tokenId) {
        try {
            Object value = redisTemplate.opsForValue().get("refresh:" + tokenId);
            return value == null ? null : value.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    public void deleteRefreshToken(String tokenId) {
        try {
            redisTemplate.delete("refresh:" + tokenId);
        } catch (Exception ignored) {
        }
    }
}
