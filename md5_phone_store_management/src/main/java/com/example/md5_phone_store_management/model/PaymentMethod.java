package com.example.md5_phone_store_management.model;

public enum PaymentMethod {
    VNPAY("VNPAY"),
    CASH("Tiền mặt");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
} 