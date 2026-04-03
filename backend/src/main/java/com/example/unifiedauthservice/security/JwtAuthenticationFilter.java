package com.example.unifiedauthservice.security;

import com.example.unifiedauthservice.service.TokenStoreService;
import com.example.unifiedauthservice.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenStoreService tokenStoreService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (jwtService.isExpired(token) || tokenStoreService.isBlacklisted(jwtService.extractTokenId(token))) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtService.extractUsername(token);
            String tenantId = jwtService.extractTenantId(token);
            String requestedTenantId = TenantContext.getTenantId();
            if (requestedTenantId != null && tenantId != null && !requestedTenantId.equalsIgnoreCase(tenantId)) {
                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            jwtService.extractRoles(token).forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            jwtService.extractPermissions(token).forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

            AuthenticatedUser principal = AuthenticatedUser.builder()
                    .username(username)
                    .tenantCode(tenantId)
                    .authorities(new ArrayList<>(authorities))
                    .enabled(true)
                    .accountNonLocked(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ignored) {
        }
        filterChain.doFilter(request, response);
    }
}
