package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import com.example.md5_phone_store_management.service.IEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmployeeService implements IEmployeeService {
    @Autowired
    private IEmployeeRepository iEmployeeRepository;

    @Override
    public Employee getEmployeeById(Integer employeeID) {
        return iEmployeeRepository.getById(employeeID);
    }

    @Override
    public int updateEmployee(Integer employeeID, String fullName, LocalDate dob, String address, String phone, String role, String email) {
        return iEmployeeRepository.updateEmployee(employeeID, fullName, dob, address, phone, role, email);
    }
}
