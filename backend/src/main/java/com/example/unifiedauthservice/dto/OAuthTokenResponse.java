package com.example.unifiedauthservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthTokenResponse {
    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;
    private String scope;
    private String tenantCode;
    private String clientId;
}
