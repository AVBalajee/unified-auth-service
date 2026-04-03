package com.example.unifiedauthservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthTokenRequest {
    @NotBlank
    private String clientId;
    @NotBlank
    private String clientSecret;
    @NotBlank
    private String grantType;
    @NotBlank
    private String tenantCode;
    private String scope;
}
