package com.example.unifiedauthservice.service;

import com.example.unifiedauthservice.dto.RoleResponse;
import com.example.unifiedauthservice.entity.Role;
import com.example.unifiedauthservice.exception.ApiException;
import com.example.unifiedauthservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleResponse> getRolesForTenant(String tenantCode) {
        return roleRepository.findByTenant_TenantCodeIgnoreCaseOrTenantIsNull(tenantCode).stream()
                .map(this::toResponse)
                .toList();
    }

    public Set<Role> resolveRoles(Set<String> roleNames, String tenantCode) {
        return roleNames.stream().map(roleName ->
                roleRepository.findByRoleNameAndTenant_TenantCodeIgnoreCase(roleName, tenantCode)
                        .or(() -> roleRepository.findByRoleNameAndTenantIsNull(roleName))
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found: " + roleName))
        ).collect(java.util.stream.Collectors.toSet());
    }

    private RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .tenantCode(role.getTenant() != null ? role.getTenant().getTenantCode() : "GLOBAL")
                .permissions(role.getPermissions().stream().map(permission -> permission.getPermissionName()).sorted().toList())
                .build();
    }
}
