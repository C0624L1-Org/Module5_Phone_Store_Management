package com.example.last.model;

public enum Gender {
    Male("Nam"),
    Female("Nữ"),
    Other("Khác");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
