package com.example.last.model;

// Enum for Role
public enum Role {
    Admin("Quản trị viên"),
    SalesStaff("Nhân viên bán hàng"),
    SalesPerson("Nhân viên kinh doanh"),
    WarehouseStaff("Nhân viên kho");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
