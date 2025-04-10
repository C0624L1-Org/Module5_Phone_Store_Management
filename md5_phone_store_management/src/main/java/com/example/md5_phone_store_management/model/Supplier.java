package com.example.md5_phone_store_management.model;

import com.example.md5_phone_store_management.listener.ChangeLogListener;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;


@Entity
@Audited
@EntityListeners(ChangeLogListener.class)
@Table(name = "supplier")
public class Supplier  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplierID")
    private Integer supplierID;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "email", length = 50)
    private String email;

    public Supplier() {}

    public Supplier(String name, String address, String phone, String email) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public Integer getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(Integer supplierID) {
        this.supplierID = supplierID;
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

    @Override
    public String toString() {
        return "Supplier{" +
                "supplierID=" + supplierID +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
