package com.example.md5_phone_store_management.model.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SupplierDTO {
    @NotBlank(message = "khong duoc de trong")
    private String name;

    @NotBlank(message = "khong duoc de trong")
    private String address;

    @Pattern(regexp="^[0-9]{10}",message = "Gom 10 so")
    private String phone;

    @Email
    @NotBlank(message = "khong duoc de trong")
    private String email;

    public SupplierDTO() {
    }

    public SupplierDTO(String name, String address, String phone, String email) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
