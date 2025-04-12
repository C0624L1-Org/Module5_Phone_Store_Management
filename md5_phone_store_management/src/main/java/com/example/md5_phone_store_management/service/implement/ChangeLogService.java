//package com.example.md5_phone_store_management.service.implement;
//
//import com.example.md5_phone_store_management.event.EntityChangeEvent;
//import com.example.md5_phone_store_management.model.ChangeLog;
//import com.example.md5_phone_store_management.model.Employee;
//import com.example.md5_phone_store_management.repository.ChangeLogRepository;
//import com.example.md5_phone_store_management.repository.IEmployeeRepository;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Sort;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//
//import java.lang.reflect.Field;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//public class ChangeLogService {
//
//    private final ChangeLogRepository changeLogRepository;
//
//    @Autowired
//    private final IEmployeeRepository employeeRepository;
//    private EntityManager entityManager;
//
//    @Autowired
//    public ChangeLogService(ChangeLogRepository changeLogRepository, IEmployeeRepository employeeRepository) {
//        this.changeLogRepository = changeLogRepository;
//        this.employeeRepository = employeeRepository;
//    }
//
//    @PersistenceContext
//    public void setEntityManager(EntityManager entityManager) {
//        this.entityManager = entityManager;
//    }
//
//    public List<ChangeLog> getAllChangeLogs() {
//        return changeLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
//    public void handleEntityChange(EntityChangeEvent event) {
//        Object entity = event.getEntity();
//        String action = event.getAction();
//        Object oldEntity = event.getOldEntity();
//
//        Long employeeId = getCurrentEmployeeId();
//        if (employeeId == null) {
//            // Không lưu log nếu không xác thực được nhân viên
//            return;
//        }
//
//        switch (action) {
//            case "INSERT":
//                String nameValue = getNameFieldValue(entity);
//                if (nameValue != null) {
//                    saveChangeLog(entity, "INSERT", "name", null, nameValue, employeeId);
//                }
//                break;
//            case "UPDATE":
//                Map<String, String> nameChange = getNameFieldChange(oldEntity, entity);
//                if (!nameChange.isEmpty()) {
//                    saveChangeLog(
//                            entity,
//                            "UPDATE",
//                            "name",
//                            nameChange.get("oldValue"),
//                            nameChange.get("newValue"),
//                            employeeId
//                    );
//                }
//                break;
//            case "DELETE":
//                String deletedNameValue = getNameFieldValue(oldEntity);
//                if (deletedNameValue != null) {
//                    saveChangeLog(entity, "DELETE", "name", deletedNameValue, null, employeeId);
//                }
//                break;
//        }
//    }
//
//    private void saveChangeLog(Object entity, String action, String fieldName, String oldValue, String newValue, Long employeeId) {
//        ChangeLog log = new ChangeLog();
//        log.setEntityName(entity.getClass().getSimpleName());
//        log.setEntityId(getEntityId(entity));
//        log.setFieldName(fieldName);
//        log.setAction(action);
//        log.setOldValue(oldValue);
//        log.setNewValue(newValue);
//        log.setEmployeeId(employeeId);
//        log.setTimestamp(LocalDateTime.now());
//        changeLogRepository.save(log);
//    }
//
//    private Long getEntityId(Object entity) {
//        try {
//            Field idField = entity.getClass().getDeclaredField("id");
//            idField.setAccessible(true);
//            return (Long) idField.get(entity);
//        } catch (Exception e) {
//            return 1L; // Thay bằng logic thực tế nếu cần
//        }
//    }
//
//    private String getNameFieldValue(Object entity) {
//        try {
//            for (Field field : entity.getClass().getDeclaredFields()) {
//                field.setAccessible(true);
//                if (field.getName().equalsIgnoreCase("name")) {
//                    Object value = field.get(entity);
//                    return value != null ? value.toString() : null;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null; // Không tìm thấy trường name/Name
//    }
//
//    private Map<String, String> getNameFieldChange(Object oldEntity, Object newEntity) {
//        Map<String, String> change = new HashMap<>();
//        try {
//            for (Field field : newEntity.getClass().getDeclaredFields()) {
//                field.setAccessible(true);
//                if (field.getName().equalsIgnoreCase("name")) {
//                    Object oldValue = field.get(oldEntity);
//                    Object newValue = field.get(newEntity);
//                    if ((oldValue == null && newValue != null) ||
//                            (oldValue != null && !oldValue.equals(newValue))) {
//                        change.put("oldValue", oldValue != null ? oldValue.toString() : null);
//                        change.put("newValue", newValue != null ? newValue.toString() : null);
//                    }
//                    break; // Thoát vòng lặp sau khi tìm thấy name/Name
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return change;
//    }
//
//    private Long getCurrentEmployeeId() {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication == null || !authentication.isAuthenticated()) {
//                return null; // Không xác thực được
//            }
//            String username = authentication.getName();
//            Optional<Employee> optionalEmployee = employeeRepository.findByUsername(username);
//            Employee employee = optionalEmployee.orElseThrow(() ->
//                    new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
//            return employee.getEmployeeID().longValue(); // Chuyển Integer thành Long
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null; // Không lưu log nếu không lấy được employeeId
//        }
//    }
//}

package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.event.EntityChangeEvent;
import com.example.md5_phone_store_management.model.ChangeLog;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.repository.ChangeLogRepository;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChangeLogService {

    private final ChangeLogRepository changeLogRepository;
    private final IEmployeeRepository employeeRepository;
    private EntityManager entityManager;

    // Mảng chứa các tên trường biểu thị "tên"
    private static final String[] NAME_FIELDS = {"name", "fullName"};

    @Autowired
    public ChangeLogService(ChangeLogRepository changeLogRepository, IEmployeeRepository employeeRepository) {
        this.changeLogRepository = changeLogRepository;
        this.employeeRepository = employeeRepository;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public LocalDateTime getLastUpdateTime(String entityName) {
        List<ChangeLog> changeLogs = changeLogRepository.findTopByEntityNameOrderByTimestampDesc(entityName);
        if (changeLogs != null && !changeLogs.isEmpty()) {
            return changeLogs.get(0).getTimestamp();
        }
        return null;
    }

    /**
     * Lấy tất cả bản ghi ChangeLog, sắp xếp theo thời gian giảm dần.
     */
    public List<ChangeLog> getAllChangeLogs() {
        return changeLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleEntityChange(EntityChangeEvent event) {
        Object entity = event.getEntity();
        String action = event.getAction();
        Object oldEntity = event.getOldEntity();

        Long employeeId = getCurrentEmployeeId();
        if (employeeId == null) {
            // Không lưu log nếu không xác thực được nhân viên
            return;
        }

        switch (action) {
            case "INSERT":
                // Lưu tất cả trường trong NAME_FIELDS nếu có
                for (String fieldName : NAME_FIELDS) {
                    String nameValue = getFieldValue(entity, fieldName);
                    if (nameValue != null) {
                        saveChangeLog(entity, "INSERT", fieldName, null, nameValue, employeeId);
                    }
                }
                break;
            case "UPDATE":
                // Kiểm tra tất cả trường để tìm thay đổi
                Map<String, Map<String, String>> changes = getFieldChanges(oldEntity, entity);
                changes.forEach((fieldName, change) -> saveChangeLog(
                        entity,
                        "UPDATE",
                        fieldName,
                        change.get("oldValue"),
                        change.get("newValue"),
                        employeeId
                ));
                break;
            case "DELETE":
                // Lưu tất cả trường trong NAME_FIELDS nếu có
                for (String fieldName : NAME_FIELDS) {
                    String deletedValue = getFieldValue(oldEntity, fieldName);
                    if (deletedValue != null) {
                        saveChangeLog(entity, "DELETE", fieldName, deletedValue, null, employeeId);
                    }
                }
                break;
        }
    }

    private void saveChangeLog(Object entity, String action, String fieldName, String oldValue, String newValue, Long employeeId) {
        ChangeLog log = new ChangeLog();
        log.setEntityName(entity.getClass().getSimpleName());
        log.setEntityId(getEntityId(entity));
        log.setFieldName(fieldName);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setEmployeeId(employeeId);
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

    private String getFieldValue(Object entity, String fieldName) {
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getName().equalsIgnoreCase(fieldName)) {
                    Object value = field.get(entity);
                    return value != null ? value.toString() : null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Không tìm thấy trường
    }

    private Map<String, Map<String, String>> getFieldChanges(Object oldEntity, Object newEntity) {
        Map<String, Map<String, String>> changes = new HashMap<>();
        try {
            for (Field field : newEntity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                // Chỉ ghi log cho các trường trong NAME_FIELDS hoặc tất cả trường nếu cần
                if (Arrays.asList(NAME_FIELDS).contains(fieldName.toLowerCase()) || true) { // Thay true bằng điều kiện nếu chỉ muốn ghi NAME_FIELDS
                    Object oldValue = field.get(oldEntity);
                    Object newValue = field.get(newEntity);
                    if ((oldValue == null && newValue != null) ||
                            (oldValue != null && !oldValue.equals(newValue))) {
                        Map<String, String> change = new HashMap<>();
                        change.put("oldValue", oldValue != null ? oldValue.toString() : null);
                        change.put("newValue", newValue != null ? newValue.toString() : null);
                        changes.put(fieldName, change);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return changes;
    }

    private Long getCurrentEmployeeId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            String username = authentication.getName();
            Optional<Employee> optionalEmployee = employeeRepository.findByUsername(username);
            Employee employee = optionalEmployee.orElseThrow(() ->
                    new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
            return employee.getEmployeeID().longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}