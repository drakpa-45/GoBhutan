package com.goBhutan.adminPanel.paymentInt.service.gateway;

import com.goBhutan.adminPanel.paymentInt.dto.BfsGatewayResponse;
import com.goBhutan.adminPanel.paymentInt.entity.PaymentTransaction;
import com.goBhutan.adminPanel.paymentInt.entity.PaymentWalletConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class BfsSecureWalletGatewayClient implements WalletGatewayClient {

    private static final DateTimeFormatter BFS_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BfsSignatureService signatureService;
    private final BfsNvpCodec nvpCodec;
    private final BfsResponseParser responseParser;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public BfsGatewayResponse initiatePayment(PaymentWalletConfig config, PaymentTransaction txn) {

        Map<String, String> fields = new HashMap<>();
        fields.put("bfs_msgType", "AR");
        fields.put("bfs_benfTxnTime", effectiveBenfTxnTime(txn));
        fields.put("bfs_orderNo", txn.getGatewayOrderNo());
        fields.put("bfs_benfId", config.getMerchantId());
        fields.put("bfs_benfBankCode", defaultString(config.getBeneficiaryBankCode(), "01"));
        fields.put("bfs_txnCurrency", txn.getCurrency());
        fields.put("bfs_txnAmount", formatAmount(txn.getAmount()));
        fields.put("bfs_paymentDesc",
                txn.getDescription() == null
                        ? "Gateway payment"
                        : txn.getDescription().replace("+", " "));

        fields.put("bfs_version", defaultString(config.getVersion(), "1.0"));
        fields.put("bfs_remitterEmail",
                txn.getRemitterEmail() == null ? "" : txn.getRemitterEmail());
        return send(config, fields);
    }

    @Override
    public BfsGatewayResponse verifyAccount(PaymentWalletConfig config,PaymentTransaction txn,String bankId,String accNo) {

        Map<String, String> fields = new HashMap<>();
        fields.put("bfs_msgType", "AE");
        fields.put("bfs_bfsTxnId", txn.getProviderTransactionId());
        fields.put("bfs_benfId", config.getMerchantId());
        fields.put("bfs_remitterBankId", bankId);
        fields.put("bfs_remitterAccNo", accNo);

        return send(config, fields);
    }

    @Override
    public BfsGatewayResponse submitOtp(PaymentWalletConfig config, PaymentTransaction txn,String otp) {

        Map<String, String> fields = new HashMap<>();

        fields.put("bfs_msgType", "DR");
        fields.put("bfs_bfsTxnId", txn.getProviderTransactionId());
        fields.put("bfs_benfId", config.getMerchantId());
        fields.put("bfs_remitterOtp", otp);

        return send(config, fields);
    }

    @Override
    public BfsGatewayResponse checkStatus(PaymentWalletConfig config,
                                          PaymentTransaction txn) {

        Map<String, String> fields = new HashMap<>();

        fields.put("bfs_msgType", "AS");
        fields.put("bfs_benfTxnTime", effectiveBenfTxnTime(txn));
        fields.put("bfs_orderNo", txn.getGatewayOrderNo());
        fields.put("bfs_benfId", config.getMerchantId());
        fields.put("bfs_benfBankCode", defaultString(config.getBeneficiaryBankCode(), "01"));
        fields.put("bfs_txnCurrency", txn.getCurrency());
        fields.put("bfs_txnAmount", formatAmount(txn.getAmount()));
        fields.put("bfs_paymentDesc", defaultString(txn.getDescription(), "Gateway payment"));
        fields.put("bfs_version", defaultString(config.getVersion(), "1.0"));

        fields.put("bfs_remitterEmail",
                txn.getRemitterEmail() == null ? "" : txn.getRemitterEmail());

        return send(config, fields);
    }

    private BfsGatewayResponse send(PaymentWalletConfig config, Map<String, String> fields) {

        String msgType = fields.get("bfs_msgType");

        String checksum = signatureService.signRequest(config, fields);
        fields.put("bfs_checkSum", checksum);

        String requestBody = fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        log.info("BFS {} REQUEST: {}", msgType, requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> response =
                restTemplate.postForEntity(config.getApiUrl(),
                        new HttpEntity<>(requestBody, headers),
                        String.class);

        String responseBody = response.getBody() == null ? "" : response.getBody();

        log.info("BFS {} RESPONSE: {}", msgType, responseBody);

        Map<String, String> decoded = nvpCodec.decode(responseBody);

        boolean signatureValid = signatureService.verifyResponse(config, decoded.get("bfs_msgType"), decoded);
        if (!signatureValid) {
            throw new RuntimeException("Invalid BFS response signature for message type " + decoded.get("bfs_msgType"));
        }

        return responseParser.parse(requestBody, responseBody, decoded);
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String effectiveBenfTxnTime(PaymentTransaction txn) {
        LocalDateTime value = txn.getCreatedAt() != null
                ? txn.getCreatedAt()
                : LocalDateTime.now();

        return value.format(BFS_TIME);
    }
}
