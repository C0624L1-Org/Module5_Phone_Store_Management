package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final IEmployeeService employeeService;

    public AuthService(IEmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public boolean register(Employee employee) {
        if (employeeService.existsByUsername(employee.getUsername()) || employeeService.existsByEmail(employee.getEmail())) {
            return false; // Tài khoản hoặc email đã tồn tại
        }
        employeeService.addEmployee(employee);
        return true;
    }

    // Trong AuthService
    public Optional<Employee> login(String username, String password) {
        Optional<Employee> employee = employeeService.findByUsername(username);
        return employee.filter(e -> e.getPassword().equals(password));
    }

}
