package com.example.md5_phone_store_management.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditlog")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "TableName", length = 128)
    private String tableName;

    @Column(name = "ChangeType", length = 50)
    private String changeType;

    @Column(name = "ChangedColumns", columnDefinition = "NVARCHAR(MAX)")
    private String changedColumns;

    @Column(name = "OldValues", columnDefinition = "NVARCHAR(MAX)")
    private String oldValues;

    @Column(name = "NewValues", columnDefinition = "NVARCHAR(MAX)")
    private String newValues;

    @Column(name = "ChangedBy", length = 128)
    private String changedBy;

    @Column(name = "ChangedDate")
    private LocalDateTime changedDate;

    // Constructors
    public AuditLog() {
    }

    public AuditLog(String tableName, String changeType, String changedColumns,
                    String oldValues, String newValues, String changedBy,
                    LocalDateTime changedDate) {
        this.tableName = tableName;
        this.changeType = changeType;
        this.changedColumns = changedColumns;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.changedBy = changedBy;
        this.changedDate = changedDate;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getChangedColumns() {
        return changedColumns;
    }

    public void setChangedColumns(String changedColumns) {
        this.changedColumns = changedColumns;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedDate() {
        return changedDate;
    }

    public void setChangedDate(LocalDateTime changedDate) {
        this.changedDate = changedDate;
    }
}