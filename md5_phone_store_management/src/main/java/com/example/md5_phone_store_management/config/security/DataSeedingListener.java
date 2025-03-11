package com.example.md5_phone_store_management.config.security;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.common.EncryptPasswordUtils;
import com.example.md5_phone_store_management.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeedingListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private IEmployeeService iEmployeeService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //them admin
        if (iEmployeeService.findByUsername("admin").isEmpty()) {
            Employee admin = new Employee();
            admin.setUsername("admin");
            admin.setPassword(EncryptPasswordUtils.encryptPasswordUtils("admin"));
            admin.setEmail("vuduytan2000@gmail.com");
            admin.setPhone("0999999999");
            admin.setFullName("Admin");
            admin.setAddress("TP.HCM");
            admin.setRole(Role.Admin);
            admin.setDob(LocalDate.parse("2000-01-01"));
            admin.setAvatar("/img/default-avt.png");
            iEmployeeService.addEmployee(admin);
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
