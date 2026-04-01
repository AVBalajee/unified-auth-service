package com.example.unifiedauthservice;

import com.example.unifiedauthservice.config.JwtProperties;
import com.example.unifiedauthservice.config.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, RateLimitProperties.class})
public class UnifiedAuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnifiedAuthServiceApplication.class, args);
    }
}
