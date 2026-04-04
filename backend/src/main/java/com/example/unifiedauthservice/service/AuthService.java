package com.example.unifiedauthservice.service;

import com.example.unifiedauthservice.dto.*;
import com.example.unifiedauthservice.entity.*;
import com.example.unifiedauthservice.exception.ApiException;
import com.example.unifiedauthservice.repository.AppUserRepository;
import com.example.unifiedauthservice.repository.RefreshTokenRepository;
import com.example.unifiedauthservice.security.AuthenticatedUser;
import com.example.unifiedauthservice.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RateLimitService rateLimitService;
    private final TokenStoreService tokenStoreService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository appUserRepository;
    private final TenantService tenantService;
    private final AuditService auditService;

    public AuthResponse login(LoginRequest request, String ipAddress) {
        if (!rateLimitService.allowLoginAttempt(request.getTenantCode(), request.getUsername(), ipAddress)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts. Please try again later.");
        }

        Tenant tenant = tenantService.getActiveTenant(request.getTenantCode());
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
            if (!principal.getTenantCode().equalsIgnoreCase(tenant.getTenantCode())) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Tenant mismatch");
            }
            AppUser user = appUserRepository.findByUsernameIgnoreCaseAndTenant_TenantCodeIgnoreCase(principal.getUsername(), principal.getTenantCode())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

            List<String> roles = user.getRoles().stream().map(Role::getRoleName).distinct().sorted().toList();
            List<String> permissions = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(Permission::getPermissionName)
                    .distinct()
                    .sorted()
                    .toList();

            String accessToken = jwtService.generateAccessToken(principal, permissions, roles);
            String refreshToken = jwtService.generateRefreshToken(principal);
            persistRefreshToken(user, tenant, refreshToken);
            auditService.log(user, tenant, "LOGIN", "User login successful", ipAddress, AuditStatus.SUCCESS);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresInSeconds(jwtService.getAccessTokenTtlSeconds())
                    .tenantCode(tenant.getTenantCode())
                    .username(user.getUsername())
                    .roles(roles)
                    .permissions(permissions)
                    .build();
        } catch (BadCredentialsException ex) {
            auditService.log(null, tenant, "LOGIN", "User login failed for " + request.getUsername(), ipAddress, AuditStatus.FAILURE);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
    }

    public AuthResponse refresh(TokenRefreshRequest request, String ipAddress) {
        String token = request.getRefreshToken();
        if (jwtService.isExpired(token) || !jwtService.isRefreshToken(token)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        String tokenId = jwtService.extractTokenId(token);
        String tokenHash = hash(token);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token not found"));
        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token revoked or expired");
        }
        if (!refreshToken.getTenant().getTenantCode().equalsIgnoreCase(request.getTenantCode())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Tenant mismatch");
        }
        String cachedHash = tokenStoreService.getRefreshTokenHash(tokenId);
        if ((cachedHash != null && !cachedHash.equals(tokenHash)) || !refreshToken.getTokenHash().equals(tokenHash)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        AppUser user = refreshToken.getUser();
        AuthenticatedUser principal = AuthenticatedUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .tenantCode(user.getTenant().getTenantCode())
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(List.of())
                .build();
        List<String> roles = user.getRoles().stream().map(Role::getRoleName).distinct().sorted().toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getPermissionName)
                .distinct()
                .sorted()
                .toList();

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        tokenStoreService.deleteRefreshToken(tokenId);

        String newAccessToken = jwtService.generateAccessToken(principal, permissions, roles);
        String newRefreshToken = jwtService.generateRefreshToken(principal);
        persistRefreshToken(user, user.getTenant(), newRefreshToken);
        auditService.log(user, user.getTenant(), "REFRESH_TOKEN", "Access token refreshed", ipAddress, AuditStatus.SUCCESS);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(jwtService.getAccessTokenTtlSeconds())
                .tenantCode(user.getTenant().getTenantCode())
                .username(user.getUsername())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    public void logout(String accessToken, LogoutRequest request, String ipAddress) {
        String rawAccessToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        String accessTokenId = jwtService.extractTokenId(rawAccessToken);
        Instant accessExpiry = jwtService.extractExpiration(rawAccessToken);
        tokenStoreService.blacklistAccessToken(accessTokenId, Duration.between(Instant.now(), accessExpiry));

        String refreshTokenValue = request.getRefreshToken();
        if (!jwtService.isExpired(refreshTokenValue) && jwtService.isRefreshToken(refreshTokenValue)) {
            String refreshTokenId = jwtService.extractTokenId(refreshTokenValue);
            refreshTokenRepository.findByTokenId(refreshTokenId).ifPresent(stored -> {
                stored.setRevoked(true);
                refreshTokenRepository.save(stored);
                tokenStoreService.deleteRefreshToken(refreshTokenId);
                auditService.log(stored.getUser(), stored.getTenant(), "LOGOUT", "User logged out", ipAddress, AuditStatus.SUCCESS);
            });
        }
    }

    public UserSummaryResponse me(String username, String tenantCode) {
        AppUser user = appUserRepository.findByUsernameIgnoreCaseAndTenant_TenantCodeIgnoreCase(username, tenantCode)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        List<String> roles = user.getRoles().stream().map(Role::getRoleName).sorted().toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getPermissionName)
                .distinct().sorted().toList();
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .tenantCode(user.getTenant().getTenantCode())
                .status(user.getStatus().name())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    public OAuthTokenResponse issueClientCredentialsToken(OAuthTokenRequest request, OAuthClient client) {
        if (!"client_credentials".equals(request.getGrantType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only client_credentials grant type is supported here");
        }
        if (request.getScope() != null && !request.getScope().isBlank()) {
            for (String scope : request.getScope().split(" ")) {
                if (!client.getScopes().contains(scope)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Requested scope not allowed: " + scope);
                }
            }
        }
        String effectiveScope = (request.getScope() == null || request.getScope().isBlank()) ? client.getScopes() : request.getScope();
        String token = jwtService.generateClientAccessToken(client.getClientId(), client.getTenant().getTenantCode(), effectiveScope);
        return OAuthTokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInSeconds(jwtService.getAccessTokenTtlSeconds())
                .scope(effectiveScope)
                .tenantCode(client.getTenant().getTenantCode())
                .clientId(client.getClientId())
                .build();
    }

    private void persistRefreshToken(AppUser user, Tenant tenant, String refreshTokenValue) {
        Claims claims = jwtService.extractAllClaims(refreshTokenValue);
        String tokenId = claims.getId();
        Instant expiry = claims.getExpiration().toInstant();
        String tokenHash = hash(refreshTokenValue);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tenant(tenant)
                .tokenId(tokenId)
                .tokenHash(tokenHash)
                .expiresAt(expiry)
                .revoked(false)
                .build();
        refreshTokenRepository.save(token);
        tokenStoreService.cacheRefreshToken(tokenId, tokenHash, Duration.between(Instant.now(), expiry));
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash token", ex);
        }
    }
}
