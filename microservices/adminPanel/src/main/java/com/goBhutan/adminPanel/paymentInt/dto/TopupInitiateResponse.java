package com.goBhutan.adminPanel.paymentInt.dto;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TopupInitiateResponse {
    private String topupRef;
    private String providerTransactionId;
    private String checkoutUrl;
    private PaymentStatus status;
    private String responseCode;
    private String responseDesc;
    private List<BfsBankItemResponse> bankList;
    private LocalDateTime expiresAt;
}
