package com.example.md5_phone_store_management.model;

public enum TransactionType {
    IN("Nhập"),
    OUT("Xuất");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
