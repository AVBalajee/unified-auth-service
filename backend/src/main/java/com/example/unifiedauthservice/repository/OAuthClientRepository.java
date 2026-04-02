package com.example.unifiedauthservice.repository;

import com.example.unifiedauthservice.entity.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OAuthClientRepository extends JpaRepository<OAuthClient, UUID> {
    Optional<OAuthClient> findByClientIdAndTenant_TenantCodeIgnoreCase(String clientId, String tenantCode);
}
