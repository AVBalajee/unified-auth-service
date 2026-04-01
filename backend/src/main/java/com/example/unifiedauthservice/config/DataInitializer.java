package com.example.unifiedauthservice.config;

import com.example.unifiedauthservice.entity.*;
import com.example.unifiedauthservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final OAuthClientRepository oAuthClientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Tenant platform = createTenantIfAbsent("PLATFORM", "Platform Tenant");
        Tenant bankA = createTenantIfAbsent("BANK_A", "Bank A");
        Tenant bankB = createTenantIfAbsent("BANK_B", "Bank B");

        Permission manageUsers = createPermissionIfAbsent("MANAGE_USERS", "Create and manage platform users");
        Permission viewUsers = createPermissionIfAbsent("VIEW_USERS", "View users");
        Permission createLc = createPermissionIfAbsent("CREATE_LC", "Create letter of credit");
        Permission approveLc = createPermissionIfAbsent("APPROVE_LC", "Approve letter of credit");
        Permission viewDashboard = createPermissionIfAbsent("VIEW_DASHBOARD", "View dashboard");
        Permission manageClients = createPermissionIfAbsent("MANAGE_CLIENTS", "Manage OAuth clients");

        createRoleIfAbsent("PLATFORM_ADMIN", "Platform administrator", platform, Set.of(manageUsers, viewUsers, viewDashboard, manageClients));
        createRoleIfAbsent("TRADE_ADMIN", "Trade finance administrator", bankA, Set.of(viewUsers, createLc, approveLc, viewDashboard));
        createRoleIfAbsent("OPS_USER", "Operations user", bankA, Set.of(viewUsers, createLc, viewDashboard));
        createRoleIfAbsent("VIEWER", "Read only user", bankB, Set.of(viewDashboard));

        createUserIfAbsent("platformadmin", "platformadmin@example.com", "Admin@123", platform, Set.of("PLATFORM_ADMIN"));
        createUserIfAbsent("tradeadmin", "tradeadmin@banka.com", "Admin@123", bankA, Set.of("TRADE_ADMIN"));
        createUserIfAbsent("opsuser", "opsuser@banka.com", "Admin@123", bankA, Set.of("OPS_USER"));
        createUserIfAbsent("viewerb", "viewerb@bankb.com", "Admin@123", bankB, Set.of("VIEWER"));

        if (oAuthClientRepository.findByClientIdAndTenant_TenantCodeIgnoreCase("trade-service", "BANK_A").isEmpty()) {
            OAuthClient client = OAuthClient.builder()
                    .clientId("trade-service")
                    .clientSecretHash(passwordEncoder.encode("trade-secret"))
                    .scopes("read write approve")
                    .grantTypes("client_credentials")
                    .redirectUri("")
                    .tenant(bankA)
                    .active(true)
                    .build();
            oAuthClientRepository.save(client);
        }
    }

    private Tenant createTenantIfAbsent(String code, String name) {
        return tenantRepository.findByTenantCodeIgnoreCase(code).orElseGet(() ->
                tenantRepository.save(Tenant.builder().tenantCode(code).tenantName(name).active(true).build()));
    }

    private Permission createPermissionIfAbsent(String name, String description) {
        return permissionRepository.findByPermissionName(name).orElseGet(() ->
                permissionRepository.save(Permission.builder().permissionName(name).description(description).build()));
    }

    private void createRoleIfAbsent(String roleName, String description, Tenant tenant, Set<Permission> permissions) {
        roleRepository.findByRoleNameAndTenant_TenantCodeIgnoreCase(roleName, tenant.getTenantCode())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName(roleName)
                        .description(description)
                        .tenant(tenant)
                        .permissions(permissions)
                        .build()));
    }

    private void createUserIfAbsent(String username, String email, String password, Tenant tenant, Set<String> roleNames) {
        if (appUserRepository.findByUsernameIgnoreCaseAndTenant_TenantCodeIgnoreCase(username, tenant.getTenantCode()).isPresent()) {
            return;
        }
        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByRoleNameAndTenant_TenantCodeIgnoreCase(name, tenant.getTenantCode()).orElseThrow())
                .collect(java.util.stream.Collectors.toSet());
        AppUser user = AppUser.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .status(UserStatus.ACTIVE)
                .tenant(tenant)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .roles(roles)
                .build();
        appUserRepository.save(user);
    }
}
