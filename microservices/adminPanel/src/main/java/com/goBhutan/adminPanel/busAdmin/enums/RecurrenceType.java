package com.goBhutan.adminPanel.busAdmin.enums;

public enum RecurrenceType {
    DAILY,          // Bus runs every day
    ALTERNATE,      // Bus runs every 2 days
    WEEKDAYS,       // Monday–Friday only
    WEEKENDS,       // Saturday & Sunday only
    CUSTOM          // Uses Bus.operatingDays set (manual)
}

