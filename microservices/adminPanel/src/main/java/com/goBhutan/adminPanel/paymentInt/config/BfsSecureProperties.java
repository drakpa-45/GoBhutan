package com.goBhutan.adminPanel.paymentInt.config;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.payment.bfs")
public class BfsSecureProperties {
    private PaymentProvider provider = PaymentProvider.BFS_SECURE;
    private String beneficiaryId = "BE10000272";
    private String beneficiaryBankCode = "01";
    private String apiUrl = "https://uatbfssecure.rma.org.bt/BFSSecure/nvpapi";
    private String callbackUrl = "http://localhost:8085/boot/api/payment-int/wallet/topup/callback";
    private String version = "1.0";
    private String privateKeyPath;
    private String privateKeyPassword;
    private String bfsPublicCertPath;
    private boolean verifyResponseSignature = true;
    private boolean active = true;
}


