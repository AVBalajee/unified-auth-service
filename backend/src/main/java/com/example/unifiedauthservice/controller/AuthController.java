package com.example.unifiedauthservice.controller;

import com.example.unifiedauthservice.dto.*;
import com.example.unifiedauthservice.security.AuthenticatedUser;
import com.example.unifiedauthservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return authService.login(request, httpServletRequest.getRemoteAddr());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody TokenRefreshRequest request, HttpServletRequest httpServletRequest) {
        return authService.refresh(request, httpServletRequest.getRemoteAddr());
    }

    @PostMapping("/logout")
    public ApiResponse logout(@RequestHeader("Authorization") String authorization,
                              @Valid @RequestBody LogoutRequest request,
                              HttpServletRequest httpServletRequest) {
        authService.logout(authorization, request, httpServletRequest.getRemoteAddr());
        return ApiResponse.builder().success(true).message("Logged out successfully").build();
    }

    @GetMapping("/me")
    public UserSummaryResponse me(Authentication authentication) {
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        return authService.me(principal.getUsername(), principal.getTenantCode());
    }
}
