package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Employee;

import java.time.LocalDate;

public interface IEmployeeService {

    Employee getEmployeeById(Integer id);

    int updateEmployee(Integer employeeID, String fullName, LocalDate dob, String address, String phone, String role, String email);
}
