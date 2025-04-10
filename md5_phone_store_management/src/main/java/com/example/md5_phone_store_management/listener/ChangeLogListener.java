package com.example.md5_phone_store_management.listener;

import com.example.md5_phone_store_management.model.ChangeLog;
import com.example.md5_phone_store_management.repository.ChangeLogRepository;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PreRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ChangeLogListener {



    private final ChangeLogRepository changeLogRepository;

    @Autowired
    @Lazy
    public ChangeLogListener(ChangeLogRepository changeLogRepository) {
        this.changeLogRepository = changeLogRepository;
        System.out.println("Đã Tiêm: " + (changeLogRepository != null));
    }

    @PrePersist
    public void onInsert(Object entity) {
        saveChangeLog(entity, "INSERT", null, entity.toString());
    }

    @PreUpdate
    public void onUpdate(Object entity) {
        // Lấy giá trị cũ từ database nếu cần
        saveChangeLog(entity, "UPDATE", "oldValue", entity.toString());
    }

    @PreRemove
    public void onDelete(Object entity) {
        saveChangeLog(entity, "DELETE", entity.toString(), null);
    }

    private void saveChangeLog(Object entity, String action, String oldValue, String newValue) {
        ChangeLog log = new ChangeLog();
        log.setEntityName(entity.getClass().getSimpleName());
        log.setEntityId(getEntityId(entity)); // Cần phương thức lấy ID từ entity
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setTimestamp(LocalDateTime.now());
        changeLogRepository.save(log);
    }

    private Long getEntityId(Object entity) {
        // Logic để lấy ID từ entity, tùy thuộc vào cấu trúc entity của bạn
        return 1L; // Ví dụ, thay bằng logic thực tế
    }
}