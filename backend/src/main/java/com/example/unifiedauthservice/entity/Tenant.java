package com.example.unifiedauthservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends BaseEntity {
    @Column(nullable = false, unique = true, length = 64)
    private String tenantCode;

    @Column(nullable = false, length = 128)
    private String tenantName;

    @Column(nullable = false)
    private boolean active;
}
