package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.Data;

@Data
public class ConfirmBookingRequest {
    private String bookingRef;
    private String paymentMethod;
}
