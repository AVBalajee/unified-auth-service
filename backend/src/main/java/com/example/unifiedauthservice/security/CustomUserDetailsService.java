package com.example.unifiedauthservice.security;

import com.example.unifiedauthservice.entity.AppUser;
import com.example.unifiedauthservice.entity.UserStatus;
import com.example.unifiedauthservice.repository.AppUserRepository;
import com.example.unifiedauthservice.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String tenantCode = TenantContext.getTenantId();
        AppUser user = tenantCode == null
                ? appUserRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"))
                : appUserRepository.findByUsernameIgnoreCaseAndTenant_TenantCodeIgnoreCase(username, tenantCode)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found for tenant"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
            role.getPermissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getPermissionName())));
        });

        return AuthenticatedUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .tenantCode(user.getTenant().getTenantCode())
                .enabled(user.getStatus() == UserStatus.ACTIVE)
                .accountNonLocked(user.getStatus() != UserStatus.LOCKED)
                .accountNonExpired(user.isAccountNonExpired())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .authorities(authorities)
                .build();
    }
}
