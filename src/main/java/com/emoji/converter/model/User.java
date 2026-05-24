package com.emoji.converter.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 200)
    private String password;

    @Column(length = 100)
    private String nickname;

    @Column(length = 200)
    private String email;

    @Column(length = 500)
    private String avatarUrl;

    @Column(name = "role_id")
    private Integer roleId = 2;

    @Column
    private Integer status = 1;

    @Column(length = 20)
    private String theme = "light";

    @Column(length = 10)
    private String language = "zh-CN";

    @Column(length = 50)
    private String timezone = "Asia/Shanghai";

    @Column(name = "mappings_count")
    private Integer mappingsCount = 0;

    @Column(name = "overrides_count")
    private Integer overridesCount = 0;

    @Column(name = "total_conversions")
    private Long totalConversions = 0L;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "last_ip", length = 50)
    private String lastIp;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (nickname == null || nickname.isEmpty()) {
            nickname = username;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void encodePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.password = encoder.encode(this.password);
    }
}