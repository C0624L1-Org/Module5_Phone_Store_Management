package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.service.implement.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private EmployeeService employeeService;

    @ModelAttribute("loggedEmployee")
    public Employee currentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            Optional<Employee> employeeOpt = employeeService.findByUsername(username);
            if (employeeOpt.isPresent()) {
                return employeeOpt.get();
            }
        }
        return null;
    }
}