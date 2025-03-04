package com.example.last.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerID;

    private String fullName;

    private String phone;

    private String address;

    private String email;

    private Date dob;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Integer purchaseCount;

    public enum Gender {
        Male, Female, Other
    }
}