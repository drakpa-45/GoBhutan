package com.goBhutan.adminPanel.paymentInt.service.gateway;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BfsSourceStringBuilder {

    public String buildRequestSourceString(String msgType, Map<String, String> fields) {
        return switch (msgType) {
            case "AR", "AS" -> join(fields,
                    "bfs_benfBankCode",
                    "bfs_benfId",
                    "bfs_benfTxnTime",
                    "bfs_msgType",
                    "bfs_orderNo",
                    "bfs_paymentDesc",
                    "bfs_remitterEmail",
                    "bfs_txnAmount",
                    "bfs_txnCurrency",
                    "bfs_version");
            case "AE" -> join(fields,
                    "bfs_benfId",
                    "bfs_bfsTxnId",
                    "bfs_msgType",
                    "bfs_remitterAccNo",
                    "bfs_remitterBankId");
            case "DR" -> join(fields,
                    "bfs_benfId",
                    "bfs_bfsTxnId",
                    "bfs_msgType",
                    "bfs_remitterOtp");
            default -> throw new IllegalArgumentException("Unsupported BFS request msgType: " + msgType);
        };
    }

    public String buildResponseSourceString(String msgType, Map<String, String> fields) {
        return switch (msgType) {
            case "RC" -> join(fields,
                    "bfs_bankList",
                    "bfs_bfsTxnId",
                    "bfs_msgType",
                    "bfs_responseCode",
                    "bfs_responseDesc");
            case "EC" -> join(fields,
                    "bfs_msgType",
                    "bfs_responseCode",
                    "bfs_responseDesc");
            case "AC" -> join(fields,
                    "bfs_benfId",
                    "bfs_benfTxnTime",
                    "bfs_bfsTxnId",
                    "bfs_bfsTxnTime",
                    "bfs_debitAuthCode",
                    "bfs_debitAuthNo",
                    "bfs_msgType",
                    "bfs_orderNo",
                    "bfs_remitterBankId",
                    "bfs_remitterName",
                    "bfs_txnAmount",
                    "bfs_txnCurrency");
            default -> throw new IllegalArgumentException("Unsupported BFS response msgType: " + msgType);
        };
    }

    private String join(Map<String, String> fields, String... keys) {
        return String.join("|", List.of(keys).stream()
                .map(key -> fields.getOrDefault(key, ""))
                .toList());
    }
}
