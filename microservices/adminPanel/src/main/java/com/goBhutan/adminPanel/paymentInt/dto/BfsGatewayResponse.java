package com.goBhutan.adminPanel.paymentInt.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BfsGatewayResponse {
    private String messageType;
    private String providerTransactionId;
    private String responseCode;
    private String responseDesc;
    private String debitAuthCode;
    private String debitAuthNo;
    private String remitterBankId;
    private String remitterName;
    private LocalDateTime providerTransactionTime;
    private List<BfsBankItemResponse> bankList;
    private Map<String, String> responseFields;
    private String rawRequest;
    private String rawResponse;

    public List<BfsBankItemResponse> safeBankList() {
        return bankList == null ? Collections.emptyList() : bankList;
    }
}
