package com.example.unifiedauthservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private int loginMaxAttempts;
    private int loginWindowSeconds;
    private int tokenMaxAttempts;
    private int tokenWindowSeconds;
}
