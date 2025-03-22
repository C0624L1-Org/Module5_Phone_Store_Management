package com.example.md5_phone_store_management.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SupplierDTO {

    private Integer supplierID;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Số điện thoại phải từ 10 đến 15 số")
    @Size(max = 15, message = "Số điện thoại không được vượt quá 15 ký tự")
    private String phone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 50, message = "Email không được vượt quá 50 ký tự")
    private String email;

    public SupplierDTO() {}

    public SupplierDTO(Integer supplierID, String name, String address, String phone, String email) {
        this.supplierID = supplierID;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    // Getters và Setters
    public Integer getSupplierID() { return supplierID; }
    public void setSupplierID(Integer supplierID) { this.supplierID = supplierID; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

//    @Override
//    public boolean supports(Class<?> clazz) {
//        return SupplierDTO.class.equals(clazz);
//    }
//
//    @Override
//    public void validate(Object target, Errors errors) {
//        SupplierDTO supplierDTO = (SupplierDTO) target;
//
//        if (!supplierDTO.getPhone().matches("^[0-9]{10,15}$")) {
//            errors.rejectValue("phone", "error.phone", "Số điện thoại phải từ 10-15 chữ số!");
//        }
//
//        if (!supplierDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
//            errors.rejectValue("email", "error.email", "Email không hợp lệ!");
//        }
//    }
}