package com.vero.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Base class for entities that need created/updated timestamps.
 *
 * Marked with {@link MappedSuperclass} so that JPA flattens these fields into
 * each subclass's table at schema generation time, rather than creating a
 * separate table for the parent. Subclasses inherit the columns directly.
 *
 * Keeping audit fields here avoids duplicating the same two columns and the
 * same lifecycle wiring across every entity. When we add a new entity that
 * needs auditing, it extends this class and gets the behaviour for free.
 *
 * Getters only — timestamps are set by JPA lifecycle callbacks, never by
 * application code.
 */
@MappedSuperclass
@Getter
public abstract class Auditable {

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
