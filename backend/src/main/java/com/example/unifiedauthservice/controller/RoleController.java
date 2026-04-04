package com.example.unifiedauthservice.controller;

import com.example.unifiedauthservice.dto.RoleResponse;
import com.example.unifiedauthservice.security.AuthenticatedUser;
import com.example.unifiedauthservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_USERS') or hasRole('PLATFORM_ADMIN')")
    public List<RoleResponse> list(Authentication authentication) {
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        return roleService.getRolesForTenant(principal.getTenantCode());
    }
}
