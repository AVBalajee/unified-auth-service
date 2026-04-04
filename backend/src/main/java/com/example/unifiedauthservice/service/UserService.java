package com.example.unifiedauthservice.service;

import com.example.unifiedauthservice.dto.CreateUserRequest;
import com.example.unifiedauthservice.dto.UserSummaryResponse;
import com.example.unifiedauthservice.entity.*;
import com.example.unifiedauthservice.exception.ApiException;
import com.example.unifiedauthservice.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;
    private final TenantService tenantService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public List<UserSummaryResponse> getUsersForTenant(String tenantCode) {
        return appUserRepository.findByTenant_TenantCodeIgnoreCase(tenantCode).stream().map(this::toSummary).toList();
    }

    public UserSummaryResponse createUser(CreateUserRequest request, String actorIp) {
        appUserRepository.findByUsernameIgnoreCaseAndTenant_TenantCodeIgnoreCase(request.getUsername(), request.getTenantCode())
                .ifPresent(existing -> { throw new ApiException(HttpStatus.CONFLICT, "Username already exists in tenant"); });

        Tenant tenant = tenantService.getActiveTenant(request.getTenantCode());
        Set<Role> roles = roleService.resolveRoles(request.getRoleNames(), tenant.getTenantCode());

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .tenant(tenant)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .roles(roles)
                .build();
        AppUser saved = appUserRepository.save(user);
        auditService.log(saved, tenant, "USER_CREATED", "Created user via admin API", actorIp, AuditStatus.SUCCESS);
        return toSummary(saved);
    }

    public AppUser getRequiredUser(String username, String tenantCode) {
        return appUserRepository.findByUsernameIgnoreCaseAndTenant_TenantCodeIgnoreCase(username, tenantCode)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public UserSummaryResponse toSummary(AppUser user) {
        List<String> roles = user.getRoles().stream().map(Role::getRoleName).sorted().toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getPermissionName)
                .distinct()
                .sorted()
                .toList();
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
}
