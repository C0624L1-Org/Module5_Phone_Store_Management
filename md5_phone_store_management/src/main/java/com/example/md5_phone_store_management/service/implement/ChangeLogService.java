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
//


//
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
//    // Updated to include invoice-specific fields
//    private static final String[] TRACKED_FIELDS = {
//            "name", "fullName", "retailPrice", // Product fields
//            "amount" // Invoice fields
//    };
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
//    public LocalDateTime getLastUpdateTime(String entityName) {
//        List<ChangeLog> changeLogs = changeLogRepository.findTopByEntityNameOrderByTimestampDesc(entityName);
//        if (changeLogs != null && !changeLogs.isEmpty()) {
//            return changeLogs.get(0).getTimestamp();
//        }
//        return null;
//    }
//
//    public ChangeLog getLatestEntityChanges(String entityName) {
//        List<ChangeLog> changeLogs = changeLogRepository.findTopByEntityNameOrderByTimestampDesc(entityName);
//        if (changeLogs != null && !changeLogs.isEmpty()) {
//            return changeLogs.get(0);
//        }
//        return null;
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
//            return;
//        }
//
//        switch (action) {
//            case "INSERT":
//            case "INSERT_PW_NO_PRICE":
//            case "INSERT_PW_PRICE":
//                // Handle product insertions
//                for (String fieldName : TRACKED_FIELDS) {
//                    String nameValue = getFieldValue(entity, fieldName);
//                    if (nameValue != null) {
//                        if (action.equals("INSERT_PW_NO_PRICE") && fieldName.equals("retailPrice")) {
//                            saveChangeLog(entity, "INSERT", "retailPrice", null, "No Retail Price", employeeId);
//                        } else {
//                            saveChangeLog(entity, "INSERT", fieldName, null, nameValue, employeeId);
//                        }
//                    }
//                }
//                if (action.equals("INSERT_PW_NO_PRICE") && !Arrays.asList(TRACKED_FIELDS).contains("retailPrice")) {
//                    saveChangeLog(entity, "INSERT", "retailPrice", null, "No Retail Price", employeeId);
//                }
//                break;
//
//            case "INSERT_INVOICE":
//                System.out.println("đã qua ok");
//                saveChangeLog(entity, "INSERT", "invoice", null, "Invoice created", employeeId);
//                break;
//
//            case "UPDATE_RETAIL_PRICE":
//                // Handle product retailPrice updates
//                String oldRetailPrice = oldEntity != null ? getFieldValue(oldEntity, "retailPrice") : null;
//                String newRetailPrice = getFieldValue(entity, "retailPrice");
//                saveChangeLog(entity, "UPDATE", "retailPrice",
//                        oldRetailPrice != null ? oldRetailPrice : "No Retail Price",
//                        newRetailPrice != null ? newRetailPrice : "No Retail Price",
//                        employeeId);
//                // Log other changed fields
//                Map<String, Map<String, String>> changes = getFieldChanges(oldEntity, entity);
//                changes.forEach((fieldName, change) -> {
//                    if (!fieldName.equals("retailPrice")) {
//                        saveChangeLog(entity, "UPDATE", fieldName,
//                                change.get("oldValue"),
//                                change.get("newValue"),
//                                employeeId);
//                    }
//                });
//                break;
//
//            case "UPDATE":
//            case "UPDATE_STOCK":
//                // Handle product updates
//                changes = getFieldChanges(oldEntity, entity);
//                changes.forEach((fieldName, change) -> saveChangeLog(
//                        entity,
//                        "UPDATE",
//                        fieldName,
//                        change.get("oldValue"),
//                        change.get("newValue"),
//                        employeeId
//                ));
//                break;
//
//            case "UPDATE_INVOICE":
//                // Handle invoice updates
//                changes = getFieldChanges(oldEntity, entity);
//                changes.forEach((fieldName, change) -> saveChangeLog(
//                        entity,
//                        "UPDATE",
//                        fieldName,
//                        change.get("oldValue"),
//                        change.get("newValue"),
//                        employeeId
//                ));
//                break;
//
//            case "DELETE_IMAGES":
//                // Handle product image deletion
//                saveChangeLog(entity, "DELETE", "images", "Images existed", "Images deleted", employeeId);
//                break;
//
//            case "DELETE_INVOICE":
//                // Handle invoice deletion
//                saveChangeLog(entity, "DELETE", "invoice", "Invoice existed", "Invoice deleted", employeeId);
//                break;
//        }
//    }
//
//    private void saveChangeLog(Object entity, String action, String fieldName, String oldValue, String newValue, Long employeeId) {
//        ChangeLog log = new ChangeLog();
//        log.setEntityName(entity.getClass().getSimpleName().toLowerCase()); // e.g., "invoice" or "product"
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
//            // Handle Product entity (productID)
//            if (entity.getClass().getSimpleName().equalsIgnoreCase("Product")) {
//                Field idField = entity.getClass().getDeclaredField("productID");
//                idField.setAccessible(true);
//                return ((Integer) idField.get(entity)).longValue();
//            }
//            // Handle Invoice entity (id)
//            else if (entity.getClass().getSimpleName().equalsIgnoreCase("Invoice")) {
//                Field idField = entity.getClass().getDeclaredField("id");
//                idField.setAccessible(true);
//                return (Long) idField.get(entity);
//            }
//            return 1L; // Default value, replace with proper logic if needed
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 1L; // Fallback, consider logging or throwing an exception
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
//        return null;
//    }
//
//    private Map<String, Map<String, String>> getFieldChanges(Object oldEntity, Object newEntity) {
//        Map<String, Map<String, String>> changes = new HashMap<>();
//        try {
//            for (Field field : newEntity.getClass().getDeclaredFields()) {
//                field.setAccessible(true);
//                String fieldName = field.getName();
//                if (Arrays.asList(TRACKED_FIELDS).contains(fieldName.toLowerCase())) {
//                    Object oldValue = oldEntity != null ? field.get(oldEntity) : null;
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
import com.example.md5_phone_store_management.model.*;
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
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChangeLogService {

    private final ChangeLogRepository changeLogRepository;
    private final IEmployeeRepository employeeRepository;
    private EntityManager entityManager;

    private static final String[] TRACKED_FIELDS = {
            "name", "fullName", "retailPrice", // Product fields
            "amount" // Invoice fields
    };

    private final Set<String> processedEvents = new HashSet<>();

    @Autowired
    public ChangeLogService(ChangeLogRepository changeLogRepository, IEmployeeRepository employeeRepository) {
        this.changeLogRepository = changeLogRepository;
        this.employeeRepository = employeeRepository;
        System.out.println("ChangeLogService initialized with deduplication");
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

        String eventKey = action + ":" + entity.getClass().getSimpleName() + ":" + getEntityId(entity);
        if (processedEvents.contains(eventKey)) {
            System.out.println("Skipping duplicate event: " + eventKey);
            return;
        }
        processedEvents.add(eventKey);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCompletion(int status) {
                processedEvents.clear();
            }
        });

        System.out.println("Handling event: action=" + action + ", entity=" + entity.getClass().getSimpleName() + ", entityId=" + getEntityId(entity));

        Long employeeId = getCurrentEmployeeId();
        if (employeeId == null) {
            System.out.println("No employee ID found, skipping ChangeLog");
            return;
        }

        switch (action) {
            case "INSERT_CUSTOMER":
                System.out.println("Processing INSERT_CUSTOMER for customer");
                String insertValue = ((Customer) entity).getFullName() != null ? ((Customer) entity).getFullName() : String.valueOf(((Customer) entity).getCustomerID());
                saveChangeLog(entity, "INSERT", "customer", null, insertValue, employeeId);
                break;
            case "UPDATE_CUSTOMER":
                System.out.println("Processing UPDATE_CUSTOMER for customer");
                String newValueCus = ((Customer) entity).getFullName() != null ? ((Customer) entity).getFullName() : String.valueOf(((Customer) entity).getCustomerID());
                String oldValue = oldEntity != null ?
                        (((Customer) oldEntity).getFullName() != null ? ((Customer) oldEntity).getFullName() : String.valueOf(((Customer) oldEntity).getCustomerID())) :
                        null;
                saveChangeLog(entity, "UPDATE", "customer", oldValue, newValueCus, employeeId);
                break;
            case "DELETE_CUSTOMER":
                System.out.println("Processing DELETE_CUSTOMER for customer");
                String deleteValue = (String) event.getMetadata().get("newValue"); // Lấy newValue từ metadata
                if (deleteValue == null) {
                    // Dự phòng nếu newValue không có
                    if (entity != null && entity instanceof Customer) {
                        deleteValue = String.valueOf(((Customer) entity).getCustomerID());
                    } else {
                        // Lấy customerId từ metadata nếu entity là null
                        deleteValue = String.valueOf(event.getMetadata().get("customerId"));
                        if (deleteValue == null) {
                            deleteValue = "unknown_customer"; // Giá trị mặc định nếu không có thông tin
                        }
                    }
                }
                saveChangeLog(entity, "DELETE", "customer", null, deleteValue, employeeId);
                break;
            case "INSERT":
                if (!entity.getClass().getSimpleName().equalsIgnoreCase("Product")) {
                    System.out.println("Skipping non-product entity for action: " + action);
                    break;
                }
                System.out.println("Processing product insertion: " + action);
                for (String fieldName : TRACKED_FIELDS) {
                    String nameValue = getFieldValue(entity, fieldName);
                    if (nameValue != null) {
                           saveChangeLog(entity, "INSERT", fieldName, null, nameValue, employeeId);

                    }
                }
                break;

            case "INSERT_PW_NO_PRICE":
            case "INSERT_PW_PRICE":
                if (entity instanceof Product) {
                    String newValue = (String) event.getMetadata().getOrDefault("newValue", "productID=unknown, retailPrice=0");
                    System.out.println("Logging product insertion: " + newValue);
                    saveChangeLog(entity, "INSERT", "retailPrice", null, newValue, employeeId);
                }
                break;

            case "INSERT_INVOICE":
            System.out.println("Processing INSERT_INVOICE for invoice");
            Invoice invoiceEntity = (Invoice) entity;
            BigDecimal amount = BigDecimal.valueOf(invoiceEntity.getAmount());
            Long customerId = Long.valueOf(invoiceEntity.getCustomer() != null ? invoiceEntity.getCustomer().getCustomerID() : null);
            String newValue = String.format("amount=%s, customerId=%s", amount.toString(), customerId != null ? customerId.toString() : "null");
            saveChangeLog(entity, "INSERT", "invoice", null, newValue, employeeId);
            break;

            case "UPDATE_INVOICE":
                System.out.println("Processing UPDATE_INVOICE for invoice");
                Map<String, Map<String, String>> changes = getFieldChanges(oldEntity, entity);
                changes.forEach((fieldName, change) -> {
                    System.out.println("Logging field change: " + fieldName + ", old=" + change.get("oldValue") + ", new=" + change.get("newValue"));
                    saveChangeLog(entity, "UPDATE", fieldName,
                            change.get("oldValue"),
                            change.get("newValue"),
                            employeeId);
                });
                if (changes.isEmpty()) {
                    saveChangeLog(entity, "UPDATE", "invoice", null, "Invoice updated", employeeId);
                }
                break;


            case "UPDATE_RETAIL_PRICE":
                if (entity instanceof Product) {
                    String oldValueP = (String) event.getMetadata().getOrDefault("oldValue", "productID=unknown, retailPrice=0");
                    String newValueP = (String) event.getMetadata().getOrDefault("newValue", "productID=unknown, retailPrice=0");
                    System.out.println("Logging retailPrice change: old=" + oldValueP + ", new=" + newValueP);
                    saveChangeLog(entity, "UPDATE", "retailPrice", oldValueP, newValueP, employeeId);
                }
                break;

            case "UPDATE":
            case "UPDATE_STOCK":
                System.out.println("Processing update: " + action);
                changes = getFieldChanges(oldEntity, entity);
                changes.forEach((fieldName, change) -> {
                    System.out.println("Logging field change: " + fieldName + ", old=" + change.get("oldValue") + ", new=" + change.get("newValue"));
                    saveChangeLog(entity, "UPDATE", fieldName,
                            change.get("oldValue"),
                            change.get("newValue"),
                            employeeId);
                });
                break;

            case "DELETE_IMAGES":
                System.out.println("Processing DELETE_IMAGES");
                saveChangeLog(entity, "DELETE", "images", "Images existed", "Images deleted", employeeId);
                break;

            case "DELETE_INVOICE":
                System.out.println("Processing DELETE_INVOICE");
                saveChangeLog(entity, "DELETE", "invoice", "Invoice existed", "Invoice deleted", employeeId);
                break;

            default:
                System.out.println("Unhandled action: " + action);
                break;
        }
    }

    private void saveChangeLog(Object entity, String action, String fieldName, String oldValue, String newValue, Long employeeId) {
        ChangeLog log = new ChangeLog();
        log.setEntityName(entity.getClass().getSimpleName().toLowerCase());
        log.setEntityId(getEntityId(entity));
        log.setFieldName(fieldName);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setEmployeeId(employeeId);
        log.setTimestamp(LocalDateTime.now());
        System.out.println("Saving ChangeLog: entityName=" + log.getEntityName() + ", entityId=" + log.getEntityId() +
                ", fieldName=" + fieldName + ", action=" + action + ", oldValue=" + oldValue + ", newValue=" + newValue);
        changeLogRepository.save(log);
    }

    private Long getEntityId(Object entity) {
        try {
            if (entity.getClass().getSimpleName().equalsIgnoreCase("Product")) {
                Field idField = entity.getClass().getDeclaredField("productID");
                idField.setAccessible(true);
                return ((Integer) idField.get(entity)).longValue();
            } else if (entity.getClass().getSimpleName().equalsIgnoreCase("Invoice")) {
                Field idField = entity.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                return (Long) idField.get(entity);
            }
            System.out.println("Unknown entity type: " + entity.getClass().getSimpleName());
            return 1L;
        } catch (Exception e) {
            System.err.println("Error getting entity ID: " + e.getMessage());
            e.printStackTrace();
            return 1L;
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
            System.err.println("Error getting field value: " + e.getMessage());
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
                if (Arrays.asList(TRACKED_FIELDS).contains(fieldName.toLowerCase())) {
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
            System.err.println("Error getting field changes: " + e.getMessage());
            e.printStackTrace();
        }
        return changes;
    }

    private Long getCurrentEmployeeId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                System.out.println("No authentication found");
                return null;
            }
            String username = authentication.getName();
            Optional<Employee> optionalEmployee = employeeRepository.findByUsername(username);
            Employee employee = optionalEmployee.orElseThrow(() ->
                    new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
            return employee.getEmployeeID().longValue();
        } catch (Exception e) {
            System.err.println("Error getting employee ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}