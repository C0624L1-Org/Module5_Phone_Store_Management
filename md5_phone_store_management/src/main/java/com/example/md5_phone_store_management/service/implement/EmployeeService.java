//package com.example.md5_phone_store_management.service.implement;
//
//import com.example.md5_phone_store_management.common.EncryptPasswordUtils;
//import com.example.md5_phone_store_management.model.Employee;
//import com.example.md5_phone_store_management.model.Role;
//import com.example.md5_phone_store_management.repository.IEmployeeRepository;
//import com.example.md5_phone_store_management.service.CloudinaryService;
//import com.example.md5_phone_store_management.service.IEmployeeService;
//import com.example.md5_phone_store_management.service.ITransactionOutService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.logging.Logger;
//
//@Service
//public class EmployeeService implements IEmployeeService {
//
//    private static final Logger logger = Logger.getLogger(EmployeeService.class.getName());
//
//    @Autowired
//    private IEmployeeRepository iEmployeeRepository;
//
//    @Autowired
//    private EncryptPasswordUtils encryptPasswordUtils;
//
//    @Autowired
//    private CloudinaryService cloudinaryService;
//
//    @Autowired
//    private ITransactionOutService iTransactionOutService;
//
//    @Override
//    public void addEmployee(Employee employee) {
//        try {
//            if (employee.getFullName() == null || employee.getFullName().trim().isEmpty()) {
//                throw new RuntimeException("Họ và tên không được để trống!");
//            }
//
//            if (!employee.getPassword().startsWith("$2a$")) {
//                employee.setPassword(EncryptPasswordUtils.encryptPasswordUtils(employee.getPassword()));
//            }
//
//            if (employee.getAvatar() == null || employee.getAvatar().isEmpty()) {
//                employee.setAvatar("/img/default-avt.png");
//            }
//
//            iEmployeeRepository.save(employee);
//        } catch (Exception e) {
//            throw new RuntimeException("Có lỗi khi thêm người dùng : " + e.getMessage());
//        }
//    }
//
//
//
//    public Page<Employee> getAllEmployeesExceptAdmin(Pageable pageable) {
//        return iEmployeeRepository.getAllEmployeesExceptAdmin(pageable);
//    }
//
//
//    //Read and search (a Đình Anh)
//    public Page<Employee> getAllEmployees(Pageable pageable) {
//        return iEmployeeRepository.getAllEmployees(pageable);
//    }
//
//    public Page<Employee> searchEmployees(String name, String phone, String role, Pageable pageable) {
//        boolean hasName = name != null && !name.isEmpty();
//        boolean hasPhone = phone != null && !phone.isEmpty();
//        boolean hasRole = role != null && !role.isEmpty();
//
//        if (hasName && hasPhone && hasRole) {
//            return iEmployeeRepository.findAllEmployeesByNameAndPhoneNumberAndRole(name, phone, role, pageable);
//        } else if (hasName && hasPhone) {
//            return iEmployeeRepository.findAllEmployeesByNameAndPhoneNumber(name, phone, pageable);
//        } else if (hasPhone && hasRole) {
//            return iEmployeeRepository.findAllEmployeesByPhoneNumberAndRole(phone, role, pageable);
//        } else if (hasName && hasRole) {
//            return iEmployeeRepository.findAllEmployeesByNameAndRole(name, role, pageable);
//        } else if (hasName) {
//            return iEmployeeRepository.findAllEmployeesByName(name, pageable);
//        } else if (hasPhone) {
//            return iEmployeeRepository.findAllEmployeesByPhoneNumber(phone, pageable);
//        } else if (hasRole) {
//            return iEmployeeRepository.findAllEmployeesByRole(role, pageable);
//        } else {
//            return iEmployeeRepository.getAllEmployees(pageable);
//        }
//    }
//
//    //Update(Tân)
//    @Override
//    public Employee getEmployeeById(Integer employeeID) {
//        return iEmployeeRepository.getById(employeeID);
//    }
//
//    @Override
//    public int updateEmployee(Integer employeeID, String fullName, LocalDate dob, String address, String phone, Role role, String email) {
//        return iEmployeeRepository.updateEmployee(employeeID, fullName, dob, address, phone, role, email);
//    }
//
//    @Override
//    public boolean existsByUsername(String username) {
//        return iEmployeeRepository.existsByUsername(username);
//    }
//
//    @Override
//    public boolean existsByEmail(String email) {
//        return iEmployeeRepository.existsByEmail(email);
//    }
//
//    @Override
//    public Optional<Employee> findByUsername(String username) {
//        return iEmployeeRepository.findByUsername(username);
//    }
//
//    @Override
//    public void deleteEmployeesById(List<Integer> employeeIDs) {
//        for (Integer employeeID : employeeIDs) {
//            System.out.println(employeeID);
//            iTransactionOutService.deleteInventoryTransactionByEmployeeID(employeeID);
//            iEmployeeRepository.deleteEmployeeById(employeeID);
//        }
//
//    }
//
//    @Override
//    public Employee updateAvatar(Integer employeeID, MultipartFile file) {
//        try {
//            String avatarUrl = cloudinaryService.uploadFile(file, "avatar");
//            Optional<Employee> optional = iEmployeeRepository.findById(employeeID);
//            if (optional.isPresent()) {
//                Employee employee = optional.get();
//                employee.setAvatar(avatarUrl);
//                return iEmployeeRepository.save(employee);
//            } else {
//                throw new RuntimeException("Employee không tồn tại với ID: " + employeeID);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Lỗi khi upload avatar", e);
//        }
//    }
//
//    // phuong thức đổi mật khẩu
//    @Override
//    public boolean changePassword(String username, String oldPassword, String newPassword) {
//        Optional<Employee> optionalEmployee = iEmployeeRepository.findByUsername(username);
//        if (optionalEmployee.isPresent()) {
//            Employee employee = optionalEmployee.get();
//
//            // Debug: In giá trị để kiểm tra
//            logger.info("Username: " + username);
//            logger.info("Encrypted Password from DB: " + employee.getPassword());
//            logger.info("Raw Old Password from Form: " + oldPassword);
//            logger.info("Checking if old password matches...");
//
//            // Kiểm tra mật khẩu cũ
//            if (!EncryptPasswordUtils.ParseEncrypt(employee.getPassword(), oldPassword)) {
//                logger.warning("Old password does not match the encrypted password!");
//                return false;
//            }
//
//            String newEncryptedPassword = EncryptPasswordUtils.encryptPasswordUtils(newPassword);
//            employee.setPassword(newEncryptedPassword);
//            iEmployeeRepository.save(employee);
//            logger.info("Password changed successfully. New encrypted password: " + newEncryptedPassword);
//            return true;
//        }
//        logger.warning("User not found: " + username);
//        return false;
//
//
//    }
//
//    @Override
//    public long countEmployee() {
//        return iEmployeeRepository.count();
//    }
//
//    @Override
//    public long countSalesStaff() {
//        return iEmployeeRepository.countSalesStaff();
//    }
//
//    @Override
//    public long countBusinessStaff() {
//        return iEmployeeRepository.countSalesPerson();
//    }
//
//    @Override
//    public long countWarehouseStaff() {
//        return iEmployeeRepository.countWarehouseStaff();
//    }
//
//    @Override
//    public List<Integer> findEmployeeIDOfDefaultAccount() {
//        List<Integer> ids = new ArrayList<>();
//        ids.add(iEmployeeRepository.findEmployeeIdByUsername("admin"));
//        ids.add(iEmployeeRepository.findEmployeeIdByUsername("salesperson"));
//        ids.add(iEmployeeRepository.findEmployeeIdByUsername("salesstaff"));
//        ids.add(iEmployeeRepository.findEmployeeIdByUsername("warehousestaff"));
//        return ids;
//    }
//}


package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.common.EncryptPasswordUtils;
import com.example.md5_phone_store_management.event.EntityChangeEvent;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import com.example.md5_phone_store_management.service.CloudinaryService;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.ITransactionOutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Dịch vụ quản lý nhân viên.
 */
@Service
public class EmployeeService implements IEmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private IEmployeeRepository iEmployeeRepository;

    @Autowired
    private EncryptPasswordUtils encryptPasswordUtils;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ITransactionOutService iTransactionOutService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Lớp ngoại lệ tùy chỉnh khi không tìm thấy nhân viên.
     */
    public static class EmployeeNotFoundException extends RuntimeException {
        public EmployeeNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Thêm một nhân viên mới vào hệ thống.
     */
    @Override
    @Transactional
    public void addEmployee(Employee employee) {
        if (employee == null) {
            logger.error("Nhân viên không được null");
            throw new IllegalArgumentException("Nhân viên không được null");
        }
        if (employee.getFullName() == null || employee.getFullName().trim().isEmpty()) {
            logger.error("Họ và tên không được để trống");
            throw new IllegalArgumentException("Họ và tên không được để trống");
        }
        if (employee.getUsername() == null || employee.getUsername().trim().isEmpty()) {
            logger.error("Tên đăng nhập không được để trống");
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (existsByUsername(employee.getUsername())) {
            logger.error("Tên đăng nhập đã tồn tại: {}", employee.getUsername());
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            logger.error("Email không được để trống");
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (existsByEmail(employee.getEmail())) {
            logger.error("Email đã tồn tại: {}", employee.getEmail());
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (employee.getPassword() == null || employee.getPassword().trim().isEmpty()) {
            logger.error("Mật khẩu không được để trống");
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }

        try {
            if (!employee.getPassword().startsWith("$2a$")) {
                employee.setPassword(encryptPasswordUtils.encryptPasswordUtils(employee.getPassword()));
            }
            if (employee.getAvatar() == null || employee.getAvatar().isEmpty()) {
                employee.setAvatar("/img/default-avt.png");
            }
            logger.info("Đang lưu nhân viên: {}", employee.getFullName());
            Employee savedEmployee = iEmployeeRepository.save(employee);
            // Gửi sự kiện INSERT_EMPLOYEE, không dùng metadata
            eventPublisher.publishEvent(new EntityChangeEvent(this, savedEmployee, "INSERT_EMPLOYEE", null));
            logger.info("Đã lưu nhân viên thành công: {}", savedEmployee.getFullName());
        } catch (Exception e) {
            logger.error("Lỗi khi thêm nhân viên: {}", e.getMessage());
            throw new RuntimeException("Có lỗi khi thêm nhân viên: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<Employee> getAllEmployeesExceptAdmin(Pageable pageable) {
        return iEmployeeRepository.getAllEmployeesExceptAdmin(pageable);
    }

    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return iEmployeeRepository.getAllEmployees(pageable);
    }

    @Override
    public Page<Employee> searchEmployees(String name, String phone, String role, Pageable pageable) {
        boolean hasName = name != null && !name.isEmpty();
        boolean hasPhone = phone != null && !phone.isEmpty();
        boolean hasRole = role != null && !role.isEmpty();

        if (hasName && hasPhone && hasRole) {
            return iEmployeeRepository.findAllEmployeesByNameAndPhoneNumberAndRole(name, phone, role, pageable);
        } else if (hasName && hasPhone) {
            return iEmployeeRepository.findAllEmployeesByNameAndPhoneNumber(name, phone, pageable);
        } else if (hasPhone && hasRole) {
            return iEmployeeRepository.findAllEmployeesByPhoneNumberAndRole(phone, role, pageable);
        } else if (hasName && hasRole) {
            return iEmployeeRepository.findAllEmployeesByNameAndRole(name, role, pageable);
        } else if (hasName) {
            return iEmployeeRepository.findAllEmployeesByName(name, pageable);
        } else if (hasPhone) {
            return iEmployeeRepository.findAllEmployeesByPhoneNumber(phone, pageable);
        } else if (hasRole) {
            return iEmployeeRepository.findAllEmployeesByRole(role, pageable);
        } else {
            return iEmployeeRepository.getAllEmployees(pageable);
        }
    }

    @Override
    public Employee getEmployeeById(Integer employeeID) {
        return iEmployeeRepository.getById(employeeID);
    }

    /**
     * Cập nhật thông tin nhân viên.
     */
    @Override
    @Transactional
    public int updateEmployee(Integer employeeID, String fullName, LocalDate dob, String address, String phone, Role role, String email) {
        if (employeeID == null) {
            logger.error("ID nhân viên không được null");
            throw new IllegalArgumentException("ID nhân viên không được null");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            logger.error("Họ và tên không được để trống");
            throw new IllegalArgumentException("Họ và tên không được để trống");
        }
        if (email == null || email.trim().isEmpty()) {
            logger.error("Email không được để trống");
            throw new IllegalArgumentException("Email không được để trống");
        }
        Optional<Employee> existingEmployee = iEmployeeRepository.findById(employeeID);
        if (existingEmployee.isEmpty()) {
            logger.error("Không tìm thấy nhân viên với ID: {}", employeeID);
            throw new EmployeeNotFoundException("Không tìm thấy nhân viên với ID: " + employeeID);
        }
        Employee oldEmployee = new Employee();
        org.springframework.beans.BeanUtils.copyProperties(existingEmployee.get(), oldEmployee);
        if (!oldEmployee.getEmail().equals(email) && existsByEmail(email)) {
            logger.error("Email đã tồn tại: {}", email);
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        logger.info("Cập nhật nhân viên ID: {}", employeeID);
        int updated = iEmployeeRepository.updateEmployee(employeeID, fullName, dob, address, phone, role, email);
        if (updated > 0) {
            Employee updatedEmployee = iEmployeeRepository.getById(employeeID);
            // Gửi sự kiện UPDATE_EMPLOYEE, không dùng metadata
            eventPublisher.publishEvent(new EntityChangeEvent(this, updatedEmployee, "UPDATE_EMPLOYEE", oldEmployee));
            logger.info("Cập nhật nhân viên thành công: {}", fullName);
        }
        return updated;
    }

    @Override
    public boolean existsByUsername(String username) {
        return iEmployeeRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return iEmployeeRepository.existsByEmail(email);
    }

    @Override
    public Optional<Employee> findByUsername(String username) {
        return iEmployeeRepository.findByUsername(username);
    }

    /**
     * Xóa danh sách nhân viên theo ID.
     */
    @Override
    @Transactional
    public void deleteEmployeesById(List<Integer> employeeIDs) {
        if (employeeIDs == null || employeeIDs.isEmpty()) {
            logger.error("Danh sách ID nhân viên không được null hoặc rỗng");
            throw new IllegalArgumentException("Danh sách ID nhân viên không được null hoặc rỗng");
        }
        List<Employee> employees = iEmployeeRepository.findAllById(employeeIDs);
        if (employees.isEmpty()) {
            logger.error("Không tìm thấy nhân viên nào với danh sách ID: {}", employeeIDs);
            throw new IllegalArgumentException("Không tìm thấy nhân viên nào với danh sách ID");
        }
        for (Employee employee : employees) {
            logger.info("Xóa giao dịch của nhân viên ID: {}", employee.getEmployeeID());
            iTransactionOutService.deleteInventoryTransactionByEmployeeID(employee.getEmployeeID());
            // Gửi sự kiện DELETE_EMPLOYEE, không dùng metadata
            eventPublisher.publishEvent(new EntityChangeEvent(this, employee, "DELETE_EMPLOYEE", null));
            logger.info("Đã gửi sự kiện DELETE_EMPLOYEE cho nhân viên: {}", employee.getFullName());
        }
        iEmployeeRepository.deleteAllById(employeeIDs);
        logger.info("Đã xóa thành công {} nhân viên", employeeIDs.size());
    }

    /**
     * Cập nhật ảnh đại diện cho nhân viên.
     */
    @Override
    @Transactional
    public Employee updateAvatar(Integer employeeID, MultipartFile file) {
        if (employeeID == null) {
            logger.error("ID nhân viên không được null");
            throw new IllegalArgumentException("ID nhân viên không được null");
        }
        if (file == null || file.isEmpty()) {
            logger.error("File ảnh không được null hoặc rỗng");
            throw new IllegalArgumentException("File ảnh không được null hoặc rỗng");
        }
        try {
            logger.info("Đang upload avatar cho nhân viên ID: {}", employeeID);
            String avatarUrl = cloudinaryService.uploadFile(file, "avatar");
            Optional<Employee> optional = iEmployeeRepository.findById(employeeID);
            if (optional.isPresent()) {
                Employee employee = optional.get();
                employee.setAvatar(avatarUrl);
                Employee updatedEmployee = iEmployeeRepository.save(employee);
                logger.info("Cập nhật avatar thành công cho nhân viên ID: {}", employeeID);
                return updatedEmployee;
            } else {
                logger.error("Không tìm thấy nhân viên với ID: {}", employeeID);
                throw new EmployeeNotFoundException("Nhân viên không tồn tại với ID: " + employeeID);
            }
        } catch (IOException e) {
            logger.error("Lỗi khi upload avatar: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi upload avatar", e);
        }
    }

    /**
     * Đổi mật khẩu của nhân viên.
     */
    @Override
    @Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || username.trim().isEmpty()) {
            logger.error("Tên đăng nhập không được để trống");
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            logger.error("Mật khẩu cũ không được để trống");
            throw new IllegalArgumentException("Mật khẩu cũ không được để trống");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            logger.error("Mật khẩu mới không được để trống");
            throw new IllegalArgumentException("Mật khẩu mới không được để trống");
        }
        Optional<Employee> optionalEmployee = iEmployeeRepository.findByUsername(username);
        if (optionalEmployee.isEmpty()) {
            logger.warn("Không tìm thấy nhân viên với tên đăng nhập: {}", username);
            return false;
        }
        Employee employee = optionalEmployee.get();
        logger.info("Kiểm tra mật khẩu cũ cho nhân viên: {}", username);
        if (!encryptPasswordUtils.ParseEncrypt(employee.getPassword(), oldPassword)) {
            logger.warn("Mật khẩu cũ không khớp cho nhân viên: {}", username);
            return false;
        }
        String newEncryptedPassword = encryptPasswordUtils.encryptPasswordUtils(newPassword);
        employee.setPassword(newEncryptedPassword);
        iEmployeeRepository.save(employee);
        logger.info("Đổi mật khẩu thành công cho nhân viên: {}", username);
        return true;
    }

    @Override
    public long countEmployee() {
        return iEmployeeRepository.count();
    }

    @Override
    public long countSalesStaff() {
        return iEmployeeRepository.countSalesStaff();
    }

    @Override
    public long countBusinessStaff() {
        return iEmployeeRepository.countSalesPerson();
    }

    @Override
    public long countWarehouseStaff() {
        return iEmployeeRepository.countWarehouseStaff();
    }

    @Override
    public List<Integer> findEmployeeIDOfDefaultAccount() {
        List<Integer> ids = new ArrayList<>();
        ids.add(iEmployeeRepository.findEmployeeIdByUsername("admin"));
        ids.add(iEmployeeRepository.findEmployeeIdByUsername("salesperson"));
        ids.add(iEmployeeRepository.findEmployeeIdByUsername("salesstaff"));
        ids.add(iEmployeeRepository.findEmployeeIdByUsername("warehousestaff"));
        return ids;
    }
}
