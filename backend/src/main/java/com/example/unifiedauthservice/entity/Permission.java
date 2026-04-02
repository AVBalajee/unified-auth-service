package com.example.unifiedauthservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String permissionName;

    @Column(length = 255)
    private String description;
}
