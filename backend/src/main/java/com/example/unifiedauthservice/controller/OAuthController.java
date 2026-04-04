package com.example.unifiedauthservice.controller;

import com.example.unifiedauthservice.dto.OAuthTokenRequest;
import com.example.unifiedauthservice.dto.OAuthTokenResponse;
import com.example.unifiedauthservice.entity.OAuthClient;
import com.example.unifiedauthservice.service.AuthService;
import com.example.unifiedauthservice.service.OAuthClientService;
import com.example.unifiedauthservice.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthClientService oAuthClientService;
    private final AuthService authService;
    private final RateLimitService rateLimitService;

    @PostMapping("/token")
    public OAuthTokenResponse token(@Valid @RequestBody OAuthTokenRequest request, HttpServletRequest httpServletRequest) {
        if (!rateLimitService.allowTokenAttempt(request.getTenantCode(), request.getClientId(), httpServletRequest.getRemoteAddr())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many token requests");
        }
        OAuthClient client = oAuthClientService.authenticateClient(request.getClientId(), request.getClientSecret(), request.getTenantCode());
        return authService.issueClientCredentialsToken(request, client);
    }
}
