package com.example.unifiedauthservice.repository;

import com.example.unifiedauthservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    List<Role> findByTenant_TenantCodeIgnoreCaseOrTenantIsNull(String tenantCode);
    Optional<Role> findByRoleNameAndTenant_TenantCodeIgnoreCase(String roleName, String tenantCode);
    Optional<Role> findByRoleNameAndTenantIsNull(String roleName);
}
