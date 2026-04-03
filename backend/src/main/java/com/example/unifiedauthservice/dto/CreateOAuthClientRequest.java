package com.example.unifiedauthservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOAuthClientRequest {
    @NotBlank
    private String clientId;
    @NotBlank
    private String clientSecret;
    @NotBlank
    private String scopes;
    @NotBlank
    private String grantTypes;
    @NotBlank
    private String tenantCode;
    private String redirectUri;
}
