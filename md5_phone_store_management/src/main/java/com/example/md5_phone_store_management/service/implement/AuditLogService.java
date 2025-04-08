package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.AuditLog;
import com.example.md5_phone_store_management.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getRecentChanges(String tableName, int limit) {
        return auditLogRepository.findTopByTableNameOrderByChangedDateDesc(tableName, limit);
    }
}
