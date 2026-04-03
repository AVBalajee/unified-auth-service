package com.example.unifiedauthservice.security;

import com.example.unifiedauthservice.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(Arrays.copyOf(keyBytes, 32));
    }

    public String generateAccessToken(AuthenticatedUser user, List<String> permissions, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(user.getUsername())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getAccessMinutes(), ChronoUnit.MINUTES)))
                .claim("userId", user.getId().toString())
                .claim("tenantId", user.getTenantCode())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("tokenType", "ACCESS")
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateClientAccessToken(String clientId, String tenantCode, String scope) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(clientId)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getAccessMinutes(), ChronoUnit.MINUTES)))
                .claim("tenantId", tenantCode)
                .claim("scope", scope)
                .claim("tokenType", "CLIENT_ACCESS")
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(user.getUsername())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getRefreshDays(), ChronoUnit.DAYS)))
                .claim("userId", user.getId().toString())
                .claim("tenantId", user.getTenantCode())
                .claim("tokenType", "REFRESH")
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractTokenId(String token) {
        return extractAllClaims(token).getId();
    }

    public String extractTenantId(String token) {
        Object tenant = extractAllClaims(token).get("tenantId");
        return tenant == null ? null : tenant.toString();
    }

    public Instant extractExpiration(String token) {
        return extractAllClaims(token).getExpiration().toInstant();
    }

    public boolean isExpired(String token) {
        return extractExpiration(token).isBefore(Instant.now());
    }

    public boolean isRefreshToken(String token) {
        Object tokenType = extractAllClaims(token).get("tokenType");
        return "REFRESH".equals(tokenType);
    }

    public List<String> extractRoles(String token) {
        Object value = extractAllClaims(token).get("roles");
        return value instanceof List<?> list ? list.stream().map(Object::toString).toList() : List.of();
    }

    public List<String> extractPermissions(String token) {
        Object value = extractAllClaims(token).get("permissions");
        return value instanceof List<?> list ? list.stream().map(Object::toString).toList() : List.of();
    }

    public long getAccessTokenTtlSeconds() {
        return jwtProperties.getAccessMinutes() * 60;
    }
}
