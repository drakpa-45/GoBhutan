package com.goBhutan.adminPanel.paymentInt.dto;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentProvider;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletConfigResponse {
    private Long id;
    private PaymentProvider provider;
    private String merchantId;
    private String apiUrl;
    private String beneficiaryBankCode;
    private String version;
    private String privateKeyPath;
    private String bfsPublicCertPath;
    private String callbackUrl;
    private Boolean active;
    private String adminUserId;
}
