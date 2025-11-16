package com.goBhutan.adminPanel.busAdmin.enums;

public enum BookingStatus {
    PENDING(101, "PENDING"),
    CONFIRMED(102, "CONFIRMED"),
    CANCELLED(103, "CANCELLED"),
    COMPLETED(104, "COMPLETED");

    private final int code;
    private final String label;

    BookingStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}

