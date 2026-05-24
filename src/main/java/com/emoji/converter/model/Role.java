package com.emoji.converter.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "roles")
@Data
public class Role {

    @Id
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String permissions;

    @Column(name = "max_mappings")
    private Integer maxMappings = 200;

    @Column(name = "max_overrides")
    private Integer maxOverrides = 100;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}