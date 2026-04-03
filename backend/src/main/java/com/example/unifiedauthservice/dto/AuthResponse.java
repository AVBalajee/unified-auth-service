package com.example.unifiedauthservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresInSeconds;
    private String tenantCode;
    private String username;
    private List<String> roles;
    private List<String> permissions;
}
