package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    List<AuditLog> findByTableName(String tableName);
    @Query("SELECT a FROM AuditLog a WHERE a.tableName = ?1 ORDER BY a.changedDate DESC")
    List<AuditLog> findTopByTableNameOrderByChangedDateDesc(String tableName, int limit);
}
