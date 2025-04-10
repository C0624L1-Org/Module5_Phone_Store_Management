package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.event.EntityChangeEvent;
import com.example.md5_phone_store_management.model.ChangeLog;
import com.example.md5_phone_store_management.repository.ChangeLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChangeLogService {

    private final ChangeLogRepository changeLogRepository;
    private EntityManager entityManager;

    @Autowired
    public ChangeLogService(ChangeLogRepository changeLogRepository) {
        this.changeLogRepository = changeLogRepository;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleEntityChange(EntityChangeEvent event) {
        Object entity = event.getEntity();
        String action = event.getAction();
        Object oldEntity = event.getOldEntity();

        switch (action) {
            case "INSERT":
                saveChangeLog(entity, "INSERT", null, entityToString(entity));
                break;
            case "UPDATE":
                Map<String, String> changes = getChanges(oldEntity, entity);
                if (!changes.isEmpty()) {
                    saveChangeLog(entity, "UPDATE", changes.get("oldValue"), changes.get("newValue"));
                }
                break;
            case "DELETE":
                saveChangeLog(entity, "DELETE", entityToString(oldEntity), null);
                break;
        }
    }

    private void saveChangeLog(Object entity, String action, String oldValue, String newValue) {
        ChangeLog log = new ChangeLog();
        log.setEntityName(entity.getClass().getSimpleName());
        log.setEntityId(getEntityId(entity));
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setTimestamp(LocalDateTime.now());
        changeLogRepository.save(log);
    }

    private Long getEntityId(Object entity) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (Long) idField.get(entity);
        } catch (Exception e) {
            return 1L; // Thay bằng logic thực tế nếu cần
        }
    }

    private String entityToString(Object entity) {
        try {
            StringBuilder result = new StringBuilder();
            for (Field field : entity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (!field.getName().equals("id")) {
                    result.append(field.getName()).append(": '").append(field.get(entity)).append("', ");
                }
            }
            return result.length() > 0 ? result.substring(0, result.length() - 2) : "";
        } catch (Exception e) {
            return entity.toString();
        }
    }

    private Map<String, String> getChanges(Object oldEntity, Object newEntity) {
        Map<String, String> changes = new HashMap<>();
        StringBuilder oldValue = new StringBuilder();
        StringBuilder newValue = new StringBuilder();

        try {
            for (Field field : newEntity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (!field.getName().equals("id")) {
                    Object oldFieldValue = field.get(oldEntity);
                    Object newFieldValue = field.get(newEntity);
                    if ((oldFieldValue == null && newFieldValue != null) ||
                            (oldFieldValue != null && !oldFieldValue.equals(newFieldValue))) {
                        oldValue.append(field.getName()).append(": '").append(oldFieldValue).append("', ");
                        newValue.append(field.getName()).append(": '").append(newFieldValue).append("', ");
                    }
                }
            }
            if (oldValue.length() > 0) {
                changes.put("oldValue", oldValue.substring(0, oldValue.length() - 2));
                changes.put("newValue", newValue.substring(0, newValue.length() - 2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return changes;
    }
}