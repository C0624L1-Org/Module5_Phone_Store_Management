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
            admin.setEmail("admin@gmail.com");
            admin.setPhone("0999999999");
            admin.setFullName("Admin");
            admin.setAddress("TP.HCM");
            admin.setRole(Role.Admin);
            admin.setDob(LocalDate.parse("2000-01-01"));
            admin.setAvatar("/img/default-avt.png");
            iEmployeeService.addEmployee(admin);
        }

        //them SalesStaff
        if (iEmployeeService.findByUsername("salesstaff").isEmpty()) {
            Employee salesstaff = new Employee();
            salesstaff.setUsername("salesstaff");
            salesstaff.setPassword(EncryptPasswordUtils.encryptPasswordUtils("salesstaff"));
            salesstaff.setEmail("salesstaff@gmail.com");
            salesstaff.setPhone("0999999998");
            salesstaff.setFullName("Sales Staff");
            salesstaff.setAddress("TP.HCM");
            salesstaff.setRole(Role.SalesStaff);
            salesstaff.setDob(LocalDate.parse("2000-01-01"));
            salesstaff.setAvatar("/img/default-avt.png");
            iEmployeeService.addEmployee(salesstaff);
        }

        //them SalesStaff
        if (iEmployeeService.findByUsername("salesperson").isEmpty()) {
            Employee salesperson = new Employee();
            salesperson.setUsername("salesperson");
            salesperson.setPassword(EncryptPasswordUtils.encryptPasswordUtils("salesperson"));
            salesperson.setEmail("salesperson@gmail.com");
            salesperson.setPhone("0999999997");
            salesperson.setFullName("Sales Person");
            salesperson.setAddress("TP.HCM");
            salesperson.setRole(Role.SalesPerson);
            salesperson.setDob(LocalDate.parse("2000-01-01"));
            salesperson.setAvatar("/img/default-avt.png");
            iEmployeeService.addEmployee(salesperson);
        }

        //them WarehouseStaff
        if (iEmployeeService.findByUsername("warehousestaff").isEmpty()) {
            Employee warehousestaff = new Employee();
            warehousestaff.setUsername("warehousestaff");
            warehousestaff.setPassword(EncryptPasswordUtils.encryptPasswordUtils("warehousestaff"));
            warehousestaff.setEmail("warehousestaff@gmail.com");
            warehousestaff.setPhone("0999999996");
            warehousestaff.setFullName("Warehouse Staff");
            warehousestaff.setAddress("TP.HCM");
            warehousestaff.setRole(Role.WarehouseStaff);
            warehousestaff.setDob(LocalDate.parse("2000-01-01"));
            warehousestaff.setAvatar("/img/default-avt.png");
            iEmployeeService.addEmployee(warehousestaff);
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
