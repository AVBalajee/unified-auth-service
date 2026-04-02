package com.example.unifiedauthservice.repository;

import com.example.unifiedauthservice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByPermissionName(String permissionName);
}
