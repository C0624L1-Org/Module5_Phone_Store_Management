package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.common.EncryptPasswordUtils;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import com.example.md5_phone_store_management.service.CloudinaryService;
import com.example.md5_phone_store_management.service.IEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService implements IEmployeeService {

    @Autowired
    private IEmployeeRepository iEmployeeRepository;

    @Autowired
    private EncryptPasswordUtils encryptPasswordUtils;

    @Autowired
    private CloudinaryService cloudinaryService;


    @Override
    public void addEmployee(Employee employee) {
        try {
            if (!employee.getPassword().startsWith("$2a$")) {
                employee.setPassword(EncryptPasswordUtils.encryptPasswordUtils(employee.getPassword()));
            }

            if (employee.getAvatar() == null || employee.getAvatar().isEmpty()) {
                employee.setAvatar("/img/default-avt.png");
            }

            iEmployeeRepository.save(employee);
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi khi thêm người dùng : " + e.getMessage());
        }
    }

    //Read and search (a Đình Anh)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return iEmployeeRepository.getAllEmployees(pageable);
    }
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

    //Update(Tân)
    @Override
    public Employee getEmployeeById(Integer employeeID) {
        return iEmployeeRepository.getById(employeeID);
    }

    @Override
    public int updateEmployee(Integer employeeID, String fullName, LocalDate dob, String address, String phone, Role role, String email) {
        return iEmployeeRepository.updateEmployee(employeeID, fullName, dob, address, phone, role, email);
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

    @Override
    public void deleteEmployeesById(List<Integer> employeeIDs) {
        for (int i = 0; i < employeeIDs.size(); i++) {
            iEmployeeRepository.deleteById(employeeIDs.get(i));
        }
        //iEmployeeRepository.deleteAllByIdInBatch(employeeIDs);
    }

    @Override
    public Employee updateAvatar(Integer employeeID, MultipartFile file) {
        try {
            String avatarUrl = cloudinaryService.uploadFile(file, "avatar");
            Optional<Employee> optional = iEmployeeRepository.findById(employeeID);
            if (optional.isPresent()) {
                Employee employee = optional.get();
                employee.setAvatar(avatarUrl);
                return iEmployeeRepository.save(employee);
            } else {
                throw new RuntimeException("Employee không tồn tại với ID: " + employeeID);
            }
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload avatar", e);
        }
    }
}
