package com.example.last.service;

import com.example.last.model.Employee;
import com.example.last.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> findAllEmployees() {
        return employeeRepository.findAll();
    }

    public void deleteEmployeeById(Long id) {
        employeeRepository.deleteById(id);
    }

}
