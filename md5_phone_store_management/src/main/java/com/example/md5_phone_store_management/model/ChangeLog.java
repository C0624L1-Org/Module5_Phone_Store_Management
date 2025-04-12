package com.example.md5_phone_store_management.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "change_log")
public class ChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false)
    private String entityName; // Tên của entity bị thay đổi (VD: "Customer", "Order")

    @Column(name = "entity_id", nullable = false)
    private Long entityId; // ID của bản ghi bị thay đổi

    @Column(name = "field_name", nullable = false)
    private String fieldName; // Tên trường bị thay đổi (do service quyết định)

    @Column(name = "action", nullable = false)
    private String action; // Hành động: "INSERT", "UPDATE", "DELETE"

    @Column(name = "old_value", length = 4000)
    private String oldValue; // Giá trị cũ

    @Column(name = "new_value", length = 4000)
    private String newValue; // Giá trị mới

    @Column(name = "employee_id")
    private Long employeeId; // ID của nhân viên thực hiện thao tác (tùy chọn)

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp; // Thời điểm thay đổi

    private static final int MAX_LENGTH = 4000; // Giới hạn độ dài tối đa

    // Constructors
    public ChangeLog() {
    }

    // Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        if (oldValue != null && oldValue.length() > MAX_LENGTH) {
            this.oldValue = oldValue.substring(0, MAX_LENGTH - 3) + "...";
        } else {
            this.oldValue = oldValue;
        }
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        if (newValue != null && newValue.length() > MAX_LENGTH) {
            this.newValue = newValue.substring(0, MAX_LENGTH - 3) + "...";
        } else {
            this.newValue = newValue;
        }
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}