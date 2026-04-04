package com.example.unifiedauthservice.controller;

import com.example.unifiedauthservice.dto.CreateUserRequest;
import com.example.unifiedauthservice.dto.UserSummaryResponse;
import com.example.unifiedauthservice.security.AuthenticatedUser;
import com.example.unifiedauthservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    public List<UserSummaryResponse> list(Authentication authentication) {
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        return userService.getUsersForTenant(principal.getTenantCode());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasRole('PLATFORM_ADMIN')")
    public UserSummaryResponse create(@Valid @RequestBody CreateUserRequest request, HttpServletRequest httpServletRequest) {
        return userService.createUser(request, httpServletRequest.getRemoteAddr());
    }
}
