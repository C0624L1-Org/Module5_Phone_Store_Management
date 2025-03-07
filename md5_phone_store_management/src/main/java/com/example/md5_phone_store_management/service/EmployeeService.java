package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    @Autowired
    IEmployeeRepository employeeRepository;
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.getAllEmployees(pageable);
    }
    public Page<Employee> searchEmployees(String name, String phone, String role, Pageable pageable) {
        boolean hasName = name != null && !name.isEmpty();
        boolean hasPhone = phone != null && !phone.isEmpty();
        boolean hasRole = role != null && !role.isEmpty();

        if (hasName && hasPhone && hasRole) {
            return employeeRepository.findAllEmployeesByNameAndPhoneNumberAndRole(name, phone, role, pageable);
        } else if (hasName && hasPhone) {
            return employeeRepository.findAllEmployeesByNameAndPhoneNumber(name, phone, pageable);
        } else if (hasPhone && hasRole) {
            return employeeRepository.findAllEmployeesByPhoneNumberAndRole(phone, role, pageable);
        } else if (hasName && hasRole) {
            return employeeRepository.findAllEmployeesByNameAndRole(name, role, pageable);
        } else if (hasName) {
            return employeeRepository.findAllEmployeesByName(name, pageable);
        } else if (hasPhone) {
            return employeeRepository.findAllEmployeesByPhoneNumber(phone, pageable);
        } else if (hasRole) {
            return employeeRepository.findAllEmployeesByRole(role, pageable);
        } else {
            return employeeRepository.getAllEmployees(pageable);
        }
    }
}
