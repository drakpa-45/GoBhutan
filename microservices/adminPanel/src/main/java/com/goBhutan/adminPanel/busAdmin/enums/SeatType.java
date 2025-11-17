package com.goBhutan.adminPanel.busAdmin.enums;

public enum SeatType {
    FRONT(1, "Front Seat"),
    WINDOW(2, "Window Seat"),
    AISLE(3, "Aisle Seat"),
    BACK(4, "Back Seat"),
    FRONT_WINDOW(5, "Front Window Seat"),
    BACK_WINDOW(6, "Back Window Seat");

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
