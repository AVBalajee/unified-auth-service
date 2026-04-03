package com.example.unifiedauthservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class RoleResponse {
    private UUID id;
    private String roleName;
    private String description;
    private String tenantCode;
    private List<String> permissions;
}
