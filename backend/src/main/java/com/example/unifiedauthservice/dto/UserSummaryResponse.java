package com.example.unifiedauthservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UserSummaryResponse {
    private UUID id;
    private String username;
    private String email;
    private String tenantCode;
    private String status;
    private List<String> roles;
    private List<String> permissions;
}
