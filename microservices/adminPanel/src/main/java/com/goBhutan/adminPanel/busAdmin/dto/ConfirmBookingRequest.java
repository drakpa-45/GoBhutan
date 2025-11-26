package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.Data;

@Data
public class ConfirmBookingRequest {
    private String paymentRef;   // payment gateway transaction id
}
