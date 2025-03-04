package com.example.last.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productID;

    private String name;

    private BigDecimal price;

    private BigDecimal purchasePrice;

    private String CPU;

    private String storage;

    private String screenSize;

    private String camera;

    private String selfie;

    @Column(columnDefinition = "TEXT")
    private String detailedDescription;

    private String image;

    private Integer stockQuantity;

    @ManyToOne
    @JoinColumn(name = "supplierID")
    private Supplier supplier;
}