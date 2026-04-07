package com.goBhutan.adminPanel.paymentInt.dto;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentProvider;
import lombok.Data;

@Data
public class WalletConfigRequest {
    private PaymentProvider provider;
    private String merchantId;
    private String apiUrl;
    private String beneficiaryBankCode;
    private String version;
    private String privateKeyPath;
    private String privateKeyPassword;
    private String bfsPublicCertPath;
    private String callbackUrl;
    private Boolean active;
}
