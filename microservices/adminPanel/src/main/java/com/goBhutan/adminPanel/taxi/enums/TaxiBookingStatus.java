package com.goBhutan.adminPanel.taxi.enums;

public enum TaxiBookingStatus {
    PENDING,            // created, awaiting driver acceptance
    DRIVER_ACCEPTED,    // driver accepted the trip
    DEPOSIT_PAID,       // reserved: deposit paid, awaiting trip
    IN_PROGRESS,        // trip underway
    COMPLETED,          // trip done, balance settled
    CANCELLED_BY_PASSENGER,
    CANCELLED_BY_DRIVER,
    REFUNDED,
    DRIVER_DECLINED   // driver rejected the request
}
