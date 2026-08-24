package com.goBhutan.adminPanel.paymentInt.service.gateway;

import com.goBhutan.adminPanel.paymentInt.dto.BfsBankItemResponse;
import com.goBhutan.adminPanel.paymentInt.dto.BfsGatewayResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class BfsResponseParser {

    private static final DateTimeFormatter BFS_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public BfsGatewayResponse parse(String rawRequest, String rawResponse, Map<String, String> fields) {
        return BfsGatewayResponse.builder()
                .messageType(fields.get("bfs_msgType"))
                .providerTransactionId(fields.get("bfs_bfsTxnId"))
                .responseCode(firstNonBlank(fields.get("bfs_responseCode"), fields.get("bfs_debitAuthCode")))
                .responseDesc(resolveResponseDesc(fields))
                .debitAuthCode(fields.get("bfs_debitAuthCode"))
                .debitAuthNo(fields.get("bfs_debitAuthNo"))
                .remitterBankId(fields.get("bfs_remitterBankId"))
                .remitterName(fields.get("bfs_remitterName"))
                .providerTransactionTime(parseDateTime(fields.get("bfs_bfsTxnTime")))
                .bankList(parseBankList(fields.get("bfs_bankList")))
                .responseFields(fields)
                .rawRequest(rawRequest)
                .rawResponse(rawResponse)
                .build();
    }

    private List<BfsBankItemResponse> parseBankList(String bankList) {
        if (bankList == null || bankList.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(bankList.split("#"))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .map(part -> part.split("~"))
                .map(parts -> BfsBankItemResponse.builder()
                        .bankId(parts.length > 0 ? parts[0] : "")
                        .bankName(parts.length > 1 ? parts[1] : "")
                        .bankStatus(parts.length > 2 ? parts[2] : "")
                        .build())
                .toList();
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value, BFS_TIME);
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private String resolveResponseDesc(Map<String, String> fields) {
        String responseDesc = fields.get("bfs_responseDesc");
        if (responseDesc != null && !responseDesc.isBlank()) {
            return responseDesc;
        }

        String debitAuthCode = fields.get("bfs_debitAuthCode");
        if (debitAuthCode == null || debitAuthCode.isBlank()) {
            return null;
        }

        return switch (debitAuthCode) {
            case "00" -> "Approved";
            case "03" -> "Invalid Beneficiary";
            case "05" -> "Beneficiary Account Closed";
            case "12" -> "Invalid Transaction";
            case "13" -> "Invalid Amount";
            case "14" -> "Invalid Remitter Account";
            case "30" -> "Transaction Not Supported Or Format Error";
            case "45", "DO" -> "Duplicate Beneficiary Order Number";
            case "47" -> "Invalid Currency";
            case "48" -> "Transaction Limit Exceeded";
            case "51" -> "Insufficient Funds";
            case "57" -> "Transaction Not Permitted";
            case "61" -> "Withdrawal Limit Exceeded";
            case "65" -> "Withdrawal Frequency Exceeded";
            case "76", "NF" -> "Transaction Not Found";
            case "80", "BC" -> "Transaction Cancelled By Customer";
            case "84" -> "Invalid Transaction Type";
            case "85" -> "Internal Error At Bank System";
            case "IM" -> "Invalid Request Received";
            case "RB" -> "Remitter Bank Blocked";
            case "IB" -> "Invalid Remitter Bank ID";
            case "TI" -> "Transaction Invalid Status";
            case "TO" -> "Transaction Time Out";
            case "UN" -> "Unknown Error";
            default -> debitAuthCode;
        };
    }
}
