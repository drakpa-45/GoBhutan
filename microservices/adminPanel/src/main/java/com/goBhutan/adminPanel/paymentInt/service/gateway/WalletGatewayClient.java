package com.goBhutan.adminPanel.paymentInt.service.gateway;

import com.goBhutan.adminPanel.paymentInt.dto.BfsGatewayResponse;
import com.goBhutan.adminPanel.paymentInt.entity.PaymentTransaction;
import com.goBhutan.adminPanel.paymentInt.entity.PaymentWalletConfig;

public interface WalletGatewayClient {
    BfsGatewayResponse initiateTopup(PaymentWalletConfig config, PaymentTransaction transaction);
    BfsGatewayResponse verifyAccount(PaymentWalletConfig config, PaymentTransaction transaction, String remitterBankId, String remitterAccNo);
    BfsGatewayResponse submitOtp(PaymentWalletConfig config, PaymentTransaction transaction, String otp);
    BfsGatewayResponse checkStatus(PaymentWalletConfig config, PaymentTransaction transaction);
}
