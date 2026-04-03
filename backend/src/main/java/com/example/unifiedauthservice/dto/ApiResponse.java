package com.example.unifiedauthservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse {
    private boolean success;
    private String message;
}
