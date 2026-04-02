package com.example.unifiedauthservice.repository;

import com.example.unifiedauthservice.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByUsernameIgnoreCase(String username);
    Optional<AppUser> findByUsernameIgnoreCaseAndTenant_TenantCodeIgnoreCase(String username, String tenantCode);
    List<AppUser> findByTenant_TenantCodeIgnoreCase(String tenantCode);
}
