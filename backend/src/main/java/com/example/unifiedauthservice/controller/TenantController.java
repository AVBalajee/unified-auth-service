package com.example.unifiedauthservice.controller;

import com.example.unifiedauthservice.dto.TenantResponse;
import com.example.unifiedauthservice.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public List<TenantResponse> getAll() {
        return tenantService.getAll();
    }

    @GetMapping("/public")
    public List<TenantResponse> getPublicTenants() {
        return tenantService.getPublicActiveTenants();
    }
}
