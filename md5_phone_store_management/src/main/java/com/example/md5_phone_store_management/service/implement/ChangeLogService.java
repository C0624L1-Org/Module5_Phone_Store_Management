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
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//public class ChangeLogService {
//
//    private final ChangeLogRepository changeLogRepository;
//    private final IEmployeeRepository employeeRepository;
//    private EntityManager entityManager;
//
//    // Mảng chứa các tên trường biểu thị "tên"
//    private static final String[] NAME_FIELDS = {"name", "fullName"};
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
//
//    public LocalDateTime getLastUpdateTime(String entityName) {
//        List<ChangeLog> changeLogs = changeLogRepository.findTopByEntityNameOrderByTimestampDesc(entityName);
//        if (changeLogs != null && !changeLogs.isEmpty()) {
//            return changeLogs.get(0).getTimestamp();
//        }
//        return null;
//    }
//
//
//
//    public ChangeLog getLatestEntityChanges(String entityName) {
//        List<ChangeLog> changeLogs = changeLogRepository.findTopByEntityNameOrderByTimestampDesc(entityName);
//        if (changeLogs != null && !changeLogs.isEmpty()) {
//            return changeLogs.get(0);
//        }
//        return null;
//    }
//    /**
//     * Lấy tất cả bản ghi ChangeLog, sắp xếp theo thời gian giảm dần.
//     */
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
//                // Lưu tất cả trường trong NAME_FIELDS nếu có
//                for (String fieldName : NAME_FIELDS) {
//                    String nameValue = getFieldValue(entity, fieldName);
//                    if (nameValue != null) {
//                        saveChangeLog(entity, "INSERT", fieldName, null, nameValue, employeeId);
//                    }
//                }
//                break;
//            case "UPDATE":
//                // Kiểm tra tất cả trường để tìm thay đổi
//                Map<String, Map<String, String>> changes = getFieldChanges(oldEntity, entity);
//                changes.forEach((fieldName, change) -> saveChangeLog(
//                        entity,
//                        "UPDATE",
//                        fieldName,
//                        change.get("oldValue"),
//                        change.get("newValue"),
//                        employeeId
//                ));
//                break;
//            case "DELETE":
//                // Lưu tất cả trường trong NAME_FIELDS nếu có
//                for (String fieldName : NAME_FIELDS) {
//                    String deletedValue = getFieldValue(oldEntity, fieldName);
//                    if (deletedValue != null) {
//                        saveChangeLog(entity, "DELETE", fieldName, deletedValue, null, employeeId);
//                    }
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
//    private String getFieldValue(Object entity, String fieldName) {
//        try {
//            for (Field field : entity.getClass().getDeclaredFields()) {
//                field.setAccessible(true);
//                if (field.getName().equalsIgnoreCase(fieldName)) {
//                    Object value = field.get(entity);
//                    return value != null ? value.toString() : null;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null; // Không tìm thấy trường
//    }
//
//    private Map<String, Map<String, String>> getFieldChanges(Object oldEntity, Object newEntity) {
//        Map<String, Map<String, String>> changes = new HashMap<>();
//        try {
//            for (Field field : newEntity.getClass().getDeclaredFields()) {
//                field.setAccessible(true);
//                String fieldName = field.getName();
//                // Chỉ ghi log cho các trường trong NAME_FIELDS hoặc tất cả trường nếu cần
//                if (Arrays.asList(NAME_FIELDS).contains(fieldName.toLowerCase()) || true) { // Thay true bằng điều kiện nếu chỉ muốn ghi NAME_FIELDS
//                    Object oldValue = field.get(oldEntity);
//                    Object newValue = field.get(newEntity);
//                    if ((oldValue == null && newValue != null) ||
//                            (oldValue != null && !oldValue.equals(newValue))) {
//                        Map<String, String> change = new HashMap<>();
//                        change.put("oldValue", oldValue != null ? oldValue.toString() : null);
//                        change.put("newValue", newValue != null ? newValue.toString() : null);
//                        changes.put(fieldName, change);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return changes;
//    }
//
//    private Long getCurrentEmployeeId() {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication == null || !authentication.isAuthenticated()) {
//                return null;
//            }
//            String username = authentication.getName();
//            Optional<Employee> optionalEmployee = employeeRepository.findByUsername(username);
//            Employee employee = optionalEmployee.orElseThrow(() ->
//                    new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
//            return employee.getEmployeeID().longValue();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
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

    private static final String[] NAME_FIELDS = {"name", "fullName", "retailPrice"};

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

    public ChangeLog getLatestEntityChanges(String entityName) {
        List<ChangeLog> changeLogs = changeLogRepository.findTopByEntityNameOrderByTimestampDesc(entityName);
        if (changeLogs != null && !changeLogs.isEmpty()) {
            return changeLogs.get(0);
        }
        return null;
    }

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
            return;
        }

        switch (action) {
            case "INSERT_PW_NO_PRICE":
                // Thêm mới sản phẩm không có retailPrice
                for (String fieldName : NAME_FIELDS) {
                    String nameValue = getFieldValue(entity, fieldName);
                    if (nameValue != null && !fieldName.equals("retailPrice")) {
                        saveChangeLog(entity, "INSERT", fieldName, null, nameValue, employeeId);
                    }
                }
                saveChangeLog(entity, "INSERT", "retailPrice", null, "No Retail Price", employeeId);
                break;
            case "INSERT_PW_PRICE":
                // Thêm mới sản phẩm có retailPrice
                for (String fieldName : NAME_FIELDS) {
                    String nameValue = getFieldValue(entity, fieldName);
                    if (nameValue != null) {
                        saveChangeLog(entity, "INSERT", fieldName, null, nameValue, employeeId);
                    }
                }
                break;
            case "UPDATE_RETAIL_PRICE":
                // Cập nhật retailPrice
                String oldRetailPrice = oldEntity != null ? getFieldValue(oldEntity, "retailPrice") : null;
                String newRetailPrice = getFieldValue(entity, "retailPrice");
                saveChangeLog(entity, "UPDATE", "retailPrice",
                        oldRetailPrice != null ? oldRetailPrice : "No Retail Price",
                        newRetailPrice != null ? newRetailPrice : "No Retail Price",
                        employeeId);
                // Ghi log các trường khác nếu có thay đổi
                Map<String, Map<String, String>> changes = getFieldChanges(oldEntity, entity);
                changes.forEach((fieldName, change) -> {
                    if (!fieldName.equals("retailPrice")) {
                        saveChangeLog(entity, "UPDATE", fieldName,
                                change.get("oldValue"),
                                change.get("newValue"),
                                employeeId);
                    }
                });
                break;
            case "UPDATE":
            case "UPDATE_STOCK":
                // Cập nhật các trường khác ngoài retailPrice
                changes = getFieldChanges(oldEntity, entity);
                changes.forEach((fieldName, change) -> saveChangeLog(
                        entity,
                        "UPDATE",
                        fieldName,
                        change.get("oldValue"),
                        change.get("newValue"),
                        employeeId
                ));
                break;
            case "DELETE_IMAGES":
                // Xóa hình ảnh
                saveChangeLog(entity, "DELETE", "images", "Images existed", "Images deleted", employeeId);
                break;
        }
    }

    private void saveChangeLog(Object entity, String action, String fieldName, String oldValue, String newValue, Long employeeId) {
        ChangeLog log = new ChangeLog();
        log.setEntityName(entity.getClass().getSimpleName().toLowerCase()); // Đặt entityName là "product"
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
            Field idField = entity.getClass().getDeclaredField("productID");
            idField.setAccessible(true);
            return ((Integer) idField.get(entity)).longValue();
        } catch (Exception e) {
            return 1L; // Giá trị mặc định, nên thay bằng logic thực tế nếu cần
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
        return null;
    }

    private Map<String, Map<String, String>> getFieldChanges(Object oldEntity, Object newEntity) {
        Map<String, Map<String, String>> changes = new HashMap<>();
        try {
            for (Field field : newEntity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                if (Arrays.asList(NAME_FIELDS).contains(fieldName.toLowerCase())) {
                    Object oldValue = oldEntity != null ? field.get(oldEntity) : null;
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