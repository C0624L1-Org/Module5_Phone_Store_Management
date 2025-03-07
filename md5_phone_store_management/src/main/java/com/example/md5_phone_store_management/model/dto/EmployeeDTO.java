package com.example.md5_phone_store_management.model.dto;

import com.example.md5_phone_store_management.model.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;

public class EmployeeDTO implements Validator {

//    private Integer employeeID;

    @NotBlank(message = "khong duoc de trong")
    @Size(min = 5,max = 40,message = "Do dai tu 5 den 40 ki tu")
    private String fullName;

//    @NotBlank(message = "khong duoc de trong")
    @DateTimeFormat(pattern="yyyy/MM/dd")
    private LocalDate dob;

    @NotBlank(message = "khong duoc de trong")
    private String address;

    @NotBlank(message = "khong duoc de trong")
    @Pattern(regexp="^0[0-9]{9}$",message = "So dien thoai gom 10 so bat dau tu 0")
    private String phone;

    @NotBlank(message = "khong duoc de trong")
    @Size(min = 5,max = 40,message = "Do dai tu 5 den 40 ki tu")
    private String username;

    @NotBlank(message = "khong duoc de trong")
    @Size(min = 6,max = 20,message = "do dai tu 6 den 20 ki tu")
    private String password;

//    @NotBlank(message = "khong duoc de trong")
    private Role role;

    @NotBlank(message = "khong duoc de trong")
    @Email
    private String email;

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {

    }

    public EmployeeDTO(String fullName, LocalDate dob,
                       String address, String phone, String username,
                       String password, Role role, String email) {
        this.fullName = fullName;
        this.dob = dob;
        this.address = address;
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }

    public EmployeeDTO() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
