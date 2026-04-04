package com.example.unifiedauthservice.service;

import com.example.unifiedauthservice.dto.TenantResponse;
import com.example.unifiedauthservice.entity.Tenant;
import com.example.unifiedauthservice.exception.ApiException;
import com.example.unifiedauthservice.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public Tenant getActiveTenant(String tenantCode) {
        Tenant tenant = tenantRepository.findByTenantCodeIgnoreCase(tenantCode)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tenant not found"));
        if (!tenant.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tenant is inactive");
        }
        return tenant;
    }

    public List<TenantResponse> getAll() {
        return tenantRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<TenantResponse> getPublicActiveTenants() {
        return tenantRepository.findAll().stream()
                .filter(Tenant::isActive)
                .map(this::toResponse)
                .toList();
    }

    private TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .tenantCode(tenant.getTenantCode())
                .tenantName(tenant.getTenantName())
                .active(tenant.isActive())
                .build();
    }
}
