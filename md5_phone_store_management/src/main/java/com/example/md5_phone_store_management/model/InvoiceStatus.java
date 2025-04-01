package com.example.md5_phone_store_management.model;

public enum InvoiceStatus {
    
    SUCCESS("Thành công"),
    FAILED("Thất bại"),
    PROCESSING("Đang xử lý");

    private final String label;

    InvoiceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
} 