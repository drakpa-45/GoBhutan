package com.goBhutan.adminPanel.taxi.enums;

public enum TaxiPaymentStatus {
    PENDING,
    DEPOSIT_HELD,       // reserved: deposit held in escrow
    FULLY_PAID,         // full fare collected
    PARTIALLY_REFUNDED,
    FULLY_REFUNDED,
    FAILED
}
