package com.example.md5_phone_store_management.model;

import com.example.md5_phone_store_management.listener.AuditListener;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditListener.class)
public abstract class AuditableEntity {
    @PrePersist
    @PreUpdate
    @PreRemove
    public void logChanges() {
        // This method is needed to trigger the listener
    }
}