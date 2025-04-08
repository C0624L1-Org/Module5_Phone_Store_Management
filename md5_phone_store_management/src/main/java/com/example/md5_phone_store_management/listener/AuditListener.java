package com.example.md5_phone_store_management.listener;

import com.example.md5_phone_store_management.model.AuditLog;
import com.example.md5_phone_store_management.repository.AuditLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;

    @Component
    public class AuditListener {

        private final AuditLogRepository auditLogRepository;

        @Autowired
        public AuditListener(@Lazy AuditLogRepository auditLogRepository) {
            this.auditLogRepository = auditLogRepository;
        }


        @PrePersist
        public void prePersist(Object entity) {
            saveAuditLog(entity, "INSERT");
        }

        @PreUpdate
        public void preUpdate(Object entity) {
            saveAuditLog(entity, "UPDATE");
        }

        @PreRemove
        public void preRemove(Object entity) {
            saveAuditLog(entity, "DELETE");
        }

        private void saveAuditLog(Object entity, String changeType) {
            AuditLog auditLog = new AuditLog();

            String tableName = entity.getClass().getSimpleName();
            String vietnameseAction = "";

            switch (changeType) {
                case "INSERT":
                    vietnameseAction = "tôi thêm dữ liệu vào bảng " + tableName;
                    break;
                case "UPDATE":
                    vietnameseAction = "tôi cập nhật dữ liệu trong bảng " + tableName;
                    break;
                case "DELETE":
                    vietnameseAction = "tôi xóa dữ liệu khỏi bảng " + tableName;
                    break;
            }

            auditLog.setTableName(tableName);
            auditLog.setChangeType(changeType);
            auditLog.setChangedColumns(vietnameseAction);

            if (changeType.equals("DELETE")) {
                auditLog.setOldValues(entity.toString());
            } else {
                auditLog.setNewValues(entity.toString());
            }

            auditLog.setChangedBy("CURRENT_USER"); // Bạn có thể thay đổi logic này
            auditLog.setChangedDate(LocalDateTime.now());

            auditLogRepository.save(auditLog);
        }
    }