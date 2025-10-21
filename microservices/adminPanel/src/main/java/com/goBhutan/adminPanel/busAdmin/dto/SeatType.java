package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.Data;

public enum SeatType {
    FRONT(1, "Front Seat"),
    WINDOW(2, "Window Seat"),
    AISLE(3, "Aisle Seat"),
    FRONT_WINDOW(4, "Front-Window Seat"),
    BACK(5, "Back Seat");

    private final int code;
    private final String label;

    SeatType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
    public static SeatType fromCode(int code) {
        for (SeatType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Invalid SeatType code: " + code);
    }
}
