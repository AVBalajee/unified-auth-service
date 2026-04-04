package com.example.unifiedauthservice.service;

import com.example.unifiedauthservice.dto.CreateOAuthClientRequest;
import com.example.unifiedauthservice.entity.OAuthClient;
import com.example.unifiedauthservice.entity.Tenant;
import com.example.unifiedauthservice.exception.ApiException;
import com.example.unifiedauthservice.repository.OAuthClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthClientService {

    private final OAuthClientRepository oAuthClientRepository;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;

    public OAuthClient create(CreateOAuthClientRequest request) {
        oAuthClientRepository.findByClientIdAndTenant_TenantCodeIgnoreCase(request.getClientId(), request.getTenantCode())
                .ifPresent(existing -> { throw new ApiException(HttpStatus.CONFLICT, "OAuth client already exists"); });

        Tenant tenant = tenantService.getActiveTenant(request.getTenantCode());
        OAuthClient client = OAuthClient.builder()
                .clientId(request.getClientId())
                .clientSecretHash(passwordEncoder.encode(request.getClientSecret()))
                .scopes(request.getScopes())
                .grantTypes(request.getGrantTypes())
                .redirectUri(request.getRedirectUri())
                .tenant(tenant)
                .active(true)
                .build();
        return oAuthClientRepository.save(client);
    }

    public OAuthClient authenticateClient(String clientId, String clientSecret, String tenantCode) {
        OAuthClient client = oAuthClientRepository.findByClientIdAndTenant_TenantCodeIgnoreCase(clientId, tenantCode)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid client credentials"));
        if (!client.isActive() || !passwordEncoder.matches(clientSecret, client.getClientSecretHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid client credentials");
        }
        return client;
    }
}
