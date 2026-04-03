package com.example.unifiedauthservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TenantResponse {
    private UUID id;
    private String tenantCode;
    private String tenantName;
    private boolean active;
}
