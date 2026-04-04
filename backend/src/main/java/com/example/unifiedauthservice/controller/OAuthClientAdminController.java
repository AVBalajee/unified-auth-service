package com.example.unifiedauthservice.controller;

import com.example.unifiedauthservice.dto.ApiResponse;
import com.example.unifiedauthservice.dto.CreateOAuthClientRequest;
import com.example.unifiedauthservice.service.OAuthClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth2/clients")
@RequiredArgsConstructor
public class OAuthClientAdminController {

    private final OAuthClientService oAuthClientService;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_CLIENTS') or hasRole('PLATFORM_ADMIN')")
    public ApiResponse create(@Valid @RequestBody CreateOAuthClientRequest request) {
        oAuthClientService.create(request);
        return ApiResponse.builder().success(true).message("OAuth client created").build();
    }
}
