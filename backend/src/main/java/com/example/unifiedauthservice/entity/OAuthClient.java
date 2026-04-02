package com.example.unifiedauthservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oauth_clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthClient extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String clientId;

    @Column(nullable = false, length = 255)
    private String clientSecretHash;

    @Column(nullable = false, length = 255)
    private String scopes;

    @Column(nullable = false, length = 255)
    private String grantTypes;

    @Column(length = 255)
    private String redirectUri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private boolean active;
}
