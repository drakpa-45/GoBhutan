package com.goBhutan.adminPanel.paymentInt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BfsBankItemResponse {
    private String bankId;
    private String bankName;
    private String bankStatus;
}
