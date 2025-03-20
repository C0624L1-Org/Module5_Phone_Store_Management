package com.example.md5_phone_store_management.model.dto;

import com.example.md5_phone_store_management.model.Role;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;

public class EmployeeDTO implements Validator {
    private Integer employeeID;
    private String fullName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    private String address;
    private String phone;
    private String username;
    private String password;
    private Role role;
    private String email;
    private String avatar;

    // Constructor
    public EmployeeDTO() {
    }

    public EmployeeDTO(Integer employeeID, String fullName, LocalDate dob, String address, String phone, String username, String password, Role role, String email, String avatar) {
        this.employeeID = employeeID;
        this.fullName = fullName;
        this.dob = dob;
        this.address = address;
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.avatar = avatar;
    }

    // Get/Set
    public Integer getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(Integer employeeID) {
        this.employeeID = employeeID;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        EmployeeDTO employee = (EmployeeDTO) target;

        // Validate username
        String username = employee.getUsername();
        if (username.trim().isEmpty()) {
            errors.rejectValue("username", "input.null");
        } else if (username.length() < 3 || username.length() > 20) {
            errors.rejectValue("username", "", "Tên tài khoản phải từ 3 đến 20 ký tự!");
        } else if (!username.matches("^[a-zA-Z0-9]*$")) {
            errors.rejectValue("username", "", "Tài khoản không chứa ký tự đặc biệt và khoảng trắng!");
        }

        // Validate password
        String password = employee.getPassword();
        if (employee.getEmployeeID() == null) {
            if (password.trim().isEmpty()) {
                errors.rejectValue("password", "input.null");
            } else if (password.length() < 6 || password.length() > 30) {
                errors.rejectValue("password", "", "Mật khẩu phải từ 6 đến 30 ký tự!");
            }
        }

        // Validate Full Name
        String fullName = employee.getFullName();
        if (fullName.trim().isEmpty()) {
            errors.rejectValue("fullName", "input.null");
        } else if (!fullName.matches("^([\\p{L}\\s]+)$")) {
            errors.rejectValue("fullName", "", "Họ và tên chỉ chứa ký tự chữ và không có ký tự đặc biệt hoặc số!");
        } else if (fullName.length() < 3 || fullName.length() > 50) {
            errors.rejectValue("fullName", "", "Họ và tên phải từ 3 đến 50 ký tự!");
        }

        // Validate dob
        if (employee.getDob() == null || !employee.getDob().isBefore(LocalDate.now())) {
            errors.rejectValue("dob", "", "Ngày sinh phải trước ngày hiện tại!");
        }

        // Validate address
        String address = employee.getAddress();
        if (address.length() < 5 || address.length() > 200) {
            errors.rejectValue("address", "", "Địa chỉ phải từ 5 đến 200 ký tự!");
        }

        // Validate phone
        if (phone == null || phone.trim().isEmpty()) {
            errors.rejectValue("phone", "input.null");
        } else if (!phone.matches("^[0-9]{10,15}$")) {
            errors.rejectValue("phone", "", "Số điện thoại phải chứa từ 10 đến 15 chữ số!");
        }

        // Validate role
        if (employee.getRole() == null) {
            errors.rejectValue("role", "input.null");
        }

        // Validate email
        String email = employee.getEmail();
        if (email == null || email.trim().isEmpty()) {
            errors.rejectValue("email", "input.null", "Email không được để trống!");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errors.rejectValue("email", "", "Email không hợp lệ!");
        }
    }
}
