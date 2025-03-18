package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IEmployeeService {

    //Create (Tuấn Anh)
    void addEmployee(Employee employee);

    //Read(a Đình Anh)
    Page<Employee> getAllEmployees(Pageable pageable);
    Page<Employee> searchEmployees(String name, String phone, String role, Pageable pageable);

    //Update(Tân)
    Employee getEmployeeById(Integer id);
    int updateEmployee(Integer employeeID, String fullName, LocalDate dob, String address, String phone, Role role, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Employee> findByUsername(String username);

    //Delete
    void deleteEmployeesById(List<Integer> employeeIDs);

    //Upload Avatar
    Employee updateAvatar(Integer employeeID, MultipartFile file);

}
