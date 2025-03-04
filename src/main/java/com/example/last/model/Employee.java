package com.example.last.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer employeeID;

    private String fullName;

    private Date dob;

    private String address;

    private String phone;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String email;

    public enum Role {
        Admin, Sales_Employee, Sales_Staff, Warehouse_Staff
//        sếp, nv kinh doanh, nv bán hàng, thủ kho
    }
}