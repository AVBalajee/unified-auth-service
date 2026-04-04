package com.example.unifiedauthservice.service;

import com.example.unifiedauthservice.entity.*;
import com.example.unifiedauthservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(AppUser user, Tenant tenant, String action, String details, String ipAddress, AuditStatus status) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .tenant(tenant)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .status(status)
                .build();
        auditLogRepository.save(log);
    }
}
