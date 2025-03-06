package com.example.md5_phone_store_management.model.dto;

import com.example.md5_phone_store_management.model.Role;
import jakarta.persistence.*;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;

public class EmployeeDTO implements Validator {

    private Integer employeeID;


    private String fullName;


    private LocalDate dob;


    private String address;


    private String phone;


    private String username;


    private String password;


    private Role role;


    private String email;

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {

    }
}
