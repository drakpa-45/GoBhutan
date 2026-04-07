package com.goBhutan.adminPanel.paymentInt.service;

import com.goBhutan.adminPanel.paymentInt.config.BfsSecureProperties;
import com.goBhutan.adminPanel.paymentInt.dto.BfsGatewayResponse;
import com.goBhutan.adminPanel.paymentInt.dto.PaymentStatusResponse;
import com.goBhutan.adminPanel.paymentInt.dto.TopupAccountInquiryRequest;
import com.goBhutan.adminPanel.paymentInt.dto.TopupAccountInquiryResponse;
import com.goBhutan.adminPanel.paymentInt.dto.TopupDebitRequest;
import com.goBhutan.adminPanel.paymentInt.dto.TopupDebitResponse;
import com.goBhutan.adminPanel.paymentInt.dto.TopupInitiateRequest;
import com.goBhutan.adminPanel.paymentInt.dto.TopupInitiateResponse;
import com.goBhutan.adminPanel.paymentInt.dto.WalletBalanceResponse;
import com.goBhutan.adminPanel.paymentInt.dto.WalletConfigResponse;
import com.goBhutan.adminPanel.paymentInt.dto.WalletLedgerItemResponse;
import com.goBhutan.adminPanel.paymentInt.entity.PaymentTransaction;
import com.goBhutan.adminPanel.paymentInt.entity.PaymentWalletConfig;
import com.goBhutan.adminPanel.paymentInt.entity.WalletAccount;
import com.goBhutan.adminPanel.paymentInt.entity.WalletLedger;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentTransactionType;
import com.goBhutan.adminPanel.paymentInt.enums.WalletLedgerType;
import com.goBhutan.adminPanel.paymentInt.repository.PaymentTransactionRepository;
import com.goBhutan.adminPanel.paymentInt.repository.WalletAccountRepository;
import com.goBhutan.adminPanel.paymentInt.repository.WalletLedgerRepository;
import com.goBhutan.adminPanel.paymentInt.service.gateway.BfsNvpCodec;
import com.goBhutan.adminPanel.paymentInt.service.gateway.WalletGatewayClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentIntegrationService {

    private final PaymentTransactionRepository transactionRepository;
    private final WalletAccountRepository walletAccountRepository;
    private final WalletLedgerRepository walletLedgerRepository;
    private final WalletGatewayClient walletGatewayClient;
    private final BfsSecureProperties bfsProperties;
    private final PaymentTransactionWriter transactionWriter;
    private final BfsNvpCodec nvpCodec;

    public WalletConfigResponse getActiveWalletConfig(String adminUserId) {
        return toWalletResponse(buildRuntimeConfig(adminUserId));
    }

    public TopupInitiateResponse initiateTopup(TopupInitiateRequest req, String userId) {
        validateAmount(req.getAmount());

        String currency = defaultCurrency(req.getCurrency());
        PaymentWalletConfig config = buildRuntimeConfig("SYSTEM");

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentRef(UUID.randomUUID().toString());
        transaction.setGatewayOrderNo(UUID.randomUUID().toString().replace("-", ""));
        transaction.setProvider(config.getProvider());
        transaction.setTransactionType(PaymentTransactionType.WALLET_TOPUP);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setAmount(req.getAmount().setScale(2, RoundingMode.HALF_UP));
        transaction.setCurrency(currency);
        transaction.setDescription(safeDescription(req.getDescription()));
        transaction.setRemitterEmail(trimToNull(req.getRemitterEmail()));
        transaction.setUserId(userId);
        transaction.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        PaymentTransaction saved = transactionWriter.saveNew(transaction);

        try {
            BfsGatewayResponse bfsResponse = walletGatewayClient.initiateTopup(config, saved);

            saved.setProviderTransactionId(bfsResponse.getProviderTransactionId());
            saved.setProviderResponseCode(bfsResponse.getResponseCode());
            saved.setProviderMessage(defaultMessage(bfsResponse.getResponseDesc(), "Authorization request accepted"));
            saved.setRawArRequest(bfsResponse.getRawRequest());
            saved.setRawRcResponse(bfsResponse.getRawResponse());
            transitionFromGatewayCode(saved, bfsResponse.getResponseCode());
            saved = transactionWriter.save(saved);

            return TopupInitiateResponse.builder()
                    .topupRef(saved.getPaymentRef())
                    .providerTransactionId(saved.getProviderTransactionId())
                    .checkoutUrl(null)
                    .status(saved.getStatus())
                    .responseCode(bfsResponse.getResponseCode())
                    .responseDesc(bfsResponse.getResponseDesc())
                    .bankList(bfsResponse.safeBankList())
                    .expiresAt(saved.getExpiresAt())
                    .build();
        } catch (RuntimeException ex) {
            saved.setStatus(PaymentStatus.FAILED);
            saved.setProviderMessage(defaultMessage(ex.getMessage(), "Unable to initiate BFS topup"));
            saved = transactionWriter.save(saved);

            return TopupInitiateResponse.builder()
                    .topupRef(saved.getPaymentRef())
                    .providerTransactionId(saved.getProviderTransactionId())
                    .checkoutUrl(null)
                    .status(saved.getStatus())
                    .responseCode(saved.getProviderResponseCode())
                    .responseDesc(saved.getProviderMessage())
                    .bankList(java.util.Collections.emptyList())
                    .expiresAt(saved.getExpiresAt())
                    .build();
        }
    }

    public TopupAccountInquiryResponse verifyTopupAccount(TopupAccountInquiryRequest req, String userId) {
        if (isBlank(req.getTopupRef())) {
            throw new RuntimeException("topupRef is required");
        }
        if (isBlank(req.getRemitterBankId())) {
            throw new RuntimeException("remitterBankId is required");
        }
        if (isBlank(req.getRemitterAccNo())) {
            throw new RuntimeException("remitterAccNo is required");
        }

        PaymentTransaction transaction = getOwnedTopup(req.getTopupRef(), userId);
        ensureInteractiveStepAllowed(transaction);

        String bankId = req.getRemitterBankId().trim().toUpperCase();
        validateBankSelection(transaction, bankId);

        PaymentWalletConfig config = buildRuntimeConfig("SYSTEM");
        BfsGatewayResponse bfsResponse = walletGatewayClient.verifyAccount(config, transaction, bankId,
                req.getRemitterAccNo().trim());

        transaction.setRemitterBankId(bankId);
        transaction.setRemitterAccNoMasked(maskAccount(req.getRemitterAccNo().trim()));
        transaction.setProviderResponseCode(bfsResponse.getResponseCode());
        transaction.setProviderMessage(defaultMessage(bfsResponse.getResponseDesc(), "Account inquiry completed"));
        transaction.setRawAeRequest(bfsResponse.getRawRequest());
        transaction.setRawEcResponse(bfsResponse.getRawResponse());
        transitionFromGatewayCode(transaction, bfsResponse.getResponseCode());
        transactionRepository.save(transaction);

        return TopupAccountInquiryResponse.builder()
                .topupRef(transaction.getPaymentRef())
                .providerTransactionId(transaction.getProviderTransactionId())
                .status(transaction.getStatus())
                .responseCode(bfsResponse.getResponseCode())
                .responseDesc(bfsResponse.getResponseDesc())
                .otpRequired("00".equals(bfsResponse.getResponseCode()))
                .remitterBankId(bankId)
                .remitterAccNoMasked(transaction.getRemitterAccNoMasked())
                .build();
    }

    public TopupDebitResponse submitTopupOtp(TopupDebitRequest req, String userId) {
        if (isBlank(req.getTopupRef())) {
            throw new RuntimeException("topupRef is required");
        }
        if (!isValidOtp(req.getOtp())) {
            throw new RuntimeException("otp must be exactly 6 digits");
        }

        PaymentTransaction transaction = getOwnedTopup(req.getTopupRef(), userId);
        ensureInteractiveStepAllowed(transaction);

        if (isBlank(transaction.getProviderTransactionId())) {
            throw new RuntimeException("Topup authorization must be initiated first");
        }
        if (isBlank(transaction.getRemitterBankId())) {
            throw new RuntimeException("Account inquiry must be completed before OTP submission");
        }

        PaymentWalletConfig config = buildRuntimeConfig("SYSTEM");
        BfsGatewayResponse bfsResponse = walletGatewayClient.submitOtp(config, transaction, req.getOtp().trim());

        transaction.setDebitRequestedAt(LocalDateTime.now());
        applyDebitResult(transaction, bfsResponse);
        transaction.setRawDrRequest(bfsResponse.getRawRequest());
        transaction.setRawAcResponse(bfsResponse.getRawResponse());
        transactionRepository.save(transaction);

        return TopupDebitResponse.builder()
                .topupRef(transaction.getPaymentRef())
                .providerTransactionId(transaction.getProviderTransactionId())
                .status(transaction.getStatus())
                .responseCode(transaction.getProviderResponseCode())
                .responseDesc(transaction.getProviderMessage())
                .debitAuthCode(transaction.getDebitAuthCode())
                .debitAuthNo(transaction.getDebitAuthNo())
                .remitterName(transaction.getRemitterName())
                .remitterBankId(transaction.getRemitterBankId())
                .build();
    }

    public WalletBalanceResponse getWalletBalance(String userId) {
        WalletAccount wallet = walletAccountRepository.findByUserId(userId)
                .orElseGet(() -> createNewWallet(userId, "BTN"));

        return WalletBalanceResponse.builder()
                .userId(wallet.getUserId())
                .currency(wallet.getCurrency())
                .balance(wallet.getBalance())
                .status(wallet.getStatus())
                .build();
    }

    public List<WalletLedgerItemResponse> getWalletLedger(String userId) {
        return walletLedgerRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(item -> WalletLedgerItemResponse.builder()
                        .id(item.getId())
                        .type(item.getType())
                        .amount(item.getAmount())
                        .balanceBefore(item.getBalanceBefore())
                        .balanceAfter(item.getBalanceAfter())
                        .referenceType(item.getReferenceType())
                        .referenceId(item.getReferenceId())
                        .remarks(item.getRemarks())
                        .createdAt(item.getCreatedAt())
                        .build())
                .toList();
    }

    public PaymentStatusResponse getTopupStatus(String paymentRef, String userId) {
        PaymentTransaction transaction = getOwnedTopup(paymentRef, userId);
        expireIfNeeded(transaction);

        if (transaction.getStatus() == PaymentStatus.PENDING && shouldPollStatus(transaction)) {
            pollGatewayStatus(transaction);
        }

        return toPaymentStatus(transaction);
    }

    private void pollGatewayStatus(PaymentTransaction transaction) {
        PaymentWalletConfig config = buildRuntimeConfig("SYSTEM");
        BfsGatewayResponse bfsResponse = walletGatewayClient.checkStatus(config, transaction);

        transaction.setStatusCheckCount(transaction.getStatusCheckCount() + 1);
        transaction.setRawAsRequest(bfsResponse.getRawRequest());
        transaction.setRawAsResponse(bfsResponse.getRawResponse());
        applyDebitResult(transaction, bfsResponse);

        if (transaction.getStatus() == PaymentStatus.PENDING && transaction.getStatusCheckCount() >= 3) {
            transaction
                    .setProviderMessage("No final BFS response after 3 status checks; manual reconciliation required");
        }
        transactionRepository.save(transaction);
    }

    private boolean shouldPollStatus(PaymentTransaction transaction) {
        return transaction.getDebitRequestedAt() != null
                && transaction.getWalletCreditedAt() == null
                && transaction.getStatusCheckCount() < 3
                && LocalDateTime.now().isAfter(transaction.getDebitRequestedAt().plusMinutes(6));
    }

    private void applyDebitResult(PaymentTransaction transaction, BfsGatewayResponse bfsResponse) {
        transaction.setProviderTransactionId(
                defaultValue(bfsResponse.getProviderTransactionId(), transaction.getProviderTransactionId()));
        transaction.setGatewayTransactionTime(
                defaultTime(bfsResponse.getProviderTransactionTime(), transaction.getGatewayTransactionTime()));
        transaction
                .setProviderResponseCode(defaultValue(bfsResponse.getDebitAuthCode(), bfsResponse.getResponseCode()));
        transaction.setProviderMessage(defaultMessage(bfsResponse.getResponseDesc(), transaction.getProviderMessage()));
        transaction.setRemitterBankId(defaultValue(bfsResponse.getRemitterBankId(), transaction.getRemitterBankId()));
        transaction.setRemitterName(defaultValue(bfsResponse.getRemitterName(), transaction.getRemitterName()));
        transaction.setDebitAuthCode(defaultValue(bfsResponse.getDebitAuthCode(), transaction.getDebitAuthCode()));
        transaction.setDebitAuthNo(defaultValue(bfsResponse.getDebitAuthNo(), transaction.getDebitAuthNo()));

        if ("00".equals(transaction.getDebitAuthCode())) {
            transaction.setStatus(PaymentStatus.SUCCESS);
            if (transaction.getWalletCreditedAt() == null) {
                creditWalletForTopup(transaction);
            }
            if (isBlank(transaction.getProviderMessage())) {
                transaction.setProviderMessage("Payment approved");
            }
            return;
        }

        if ("00".equals(bfsResponse.getResponseCode())) {
            transaction.setStatus(PaymentStatus.PENDING);
            if (isBlank(transaction.getProviderMessage())) {
                transaction.setProviderMessage("Processing");
            }
            return;
        }

        transaction.setStatus(PaymentStatus.FAILED);
    }

    private PaymentTransaction getOwnedTopup(String paymentRef, String userId) {
        PaymentTransaction transaction = transactionRepository.findByPaymentRef(paymentRef)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found"));

        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized payment status access");
        }
        if (transaction.getTransactionType() != PaymentTransactionType.WALLET_TOPUP) {
            throw new RuntimeException("Unsupported transaction type");
        }
        return transaction;
    }

    private void ensureInteractiveStepAllowed(PaymentTransaction transaction) {
        if (expireIfNeeded(transaction)) {
            throw new RuntimeException("Topup session expired");
        }
    }

    private boolean expireIfNeeded(PaymentTransaction transaction) {
        if (transaction.getExpiresAt() != null
                && transaction.getExpiresAt().isBefore(LocalDateTime.now())
                && transaction.getStatus() != PaymentStatus.SUCCESS) {
            transaction.setStatus(PaymentStatus.EXPIRED);
            transaction.setProviderMessage("Topup session expired");
            transactionRepository.save(transaction);
            return true;
        }
        return false;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new RuntimeException("amount must be greater than zero");
        }
    }

    private void validateBankSelection(PaymentTransaction transaction, String bankId) {
        Set<String> offeredBanks = extractOfferedBankIds(transaction);
        if (!offeredBanks.isEmpty() && !offeredBanks.contains(bankId)) {
            throw new RuntimeException("Selected remitter bank was not offered for this topup session");
        }
    }

    private Set<String> extractOfferedBankIds(PaymentTransaction transaction) {
        if (isBlank(transaction.getRawRcResponse())) {
            return Collections.emptySet();
        }
        String encodedBankList = nvpCodec.decode(transaction.getRawRcResponse()).get("bfs_bankList");
        if (isBlank(encodedBankList)) {
            return Collections.emptySet();
        }
        return Arrays.stream(encodedBankList.split("#"))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .map(part -> part.split("~"))
                .filter(parts -> parts.length > 0 && !isBlank(parts[0]))
                .map(parts -> parts[0].trim().toUpperCase())
                .collect(Collectors.toSet());
    }

    private boolean isValidOtp(String otp) {
        return otp != null && otp.matches("\\d{6}");
    }

    private void creditWalletForTopup(PaymentTransaction transaction) {
        WalletAccount wallet = walletAccountRepository.findByUserIdForUpdate(transaction.getUserId())
                .orElseGet(() -> createNewWallet(transaction.getUserId(), transaction.getCurrency()));

        BigDecimal before = wallet.getBalance();
        BigDecimal after = before.add(transaction.getAmount());
        wallet.setBalance(after);
        walletAccountRepository.save(wallet);

        WalletLedger ledger = new WalletLedger();
        ledger.setUserId(transaction.getUserId());
        ledger.setType(WalletLedgerType.CREDIT);
        ledger.setAmount(transaction.getAmount());
        ledger.setBalanceBefore(before);
        ledger.setBalanceAfter(after);
        ledger.setReferenceType("TOPUP");
        ledger.setReferenceId(transaction.getPaymentRef());
        ledger.setRemarks("Wallet topup via BFS Secure");
        walletLedgerRepository.save(ledger);

        transaction.setWalletCreditedAt(LocalDateTime.now());
    }

    private WalletAccount createNewWallet(String userId, String currency) {
        WalletAccount wallet = new WalletAccount();
        wallet.setUserId(userId);
        wallet.setCurrency(defaultCurrency(currency));
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus("ACTIVE");
        return walletAccountRepository.save(wallet);
    }

    private void transitionFromGatewayCode(PaymentTransaction transaction, String responseCode) {
        transaction.setStatus("00".equals(responseCode) ? PaymentStatus.PENDING : PaymentStatus.FAILED);
    }

    private PaymentWalletConfig buildRuntimeConfig(String adminUserId) {
        PaymentWalletConfig config = new PaymentWalletConfig();
        config.setProvider(bfsProperties.getProvider());
        config.setMerchantId(bfsProperties.getBeneficiaryId());
        config.setApiUrl(bfsProperties.getApiUrl());
        config.setBeneficiaryBankCode(bfsProperties.getBeneficiaryBankCode());
        config.setVersion(bfsProperties.getVersion());
        config.setPrivateKeyPath(bfsProperties.getPrivateKeyPath());
        config.setPrivateKeyPassword(bfsProperties.getPrivateKeyPassword());
        config.setBfsPublicCertPath(bfsProperties.getBfsPublicCertPath());
        config.setCallbackUrl(bfsProperties.getCallbackUrl());
        config.setActive(bfsProperties.isActive());
        config.setAdminUserId(adminUserId);
        return config;
    }

    private WalletConfigResponse toWalletResponse(PaymentWalletConfig config) {
        return WalletConfigResponse.builder()
                .id(config.getId())
                .provider(config.getProvider())
                .merchantId(config.getMerchantId())
                .apiUrl(config.getApiUrl())
                .beneficiaryBankCode(config.getBeneficiaryBankCode())
                .version(config.getVersion())
                .privateKeyPath(config.getPrivateKeyPath())
                .bfsPublicCertPath(config.getBfsPublicCertPath())
                .callbackUrl(config.getCallbackUrl())
                .active(config.getActive())
                .adminUserId(config.getAdminUserId())
                .build();
    }

    private PaymentStatusResponse toPaymentStatus(PaymentTransaction tx) {
        return PaymentStatusResponse.builder()
                .paymentRef(tx.getPaymentRef())
                .providerTransactionId(tx.getProviderTransactionId())
                .provider(tx.getProvider())
                .transactionType(tx.getTransactionType())
                .status(tx.getStatus())
                .responseCode(tx.getProviderResponseCode())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .message(tx.getProviderMessage())
                .remitterBankId(tx.getRemitterBankId())
                .remitterName(tx.getRemitterName())
                .debitAuthCode(tx.getDebitAuthCode())
                .debitAuthNo(tx.getDebitAuthNo())
                .statusCheckCount(tx.getStatusCheckCount())
                .walletCreditedAt(tx.getWalletCreditedAt())
                .build();
    }

    private String defaultCurrency(String currency) {
        return isBlank(currency) ? "BTN" : currency.trim().toUpperCase();
    }

    private String safeDescription(String value) {
        String description = isBlank(value) ? "Wallet topup" : value.trim();
        return description.length() > 30 ? description.substring(0, 30) : description;
    }

    private String maskAccount(String accountNo) {
        if (accountNo.length() <= 4) {
            return accountNo;
        }
        return "*".repeat(accountNo.length() - 4) + accountNo.substring(accountNo.length() - 4);
    }

    private String defaultMessage(String primary, String fallback) {
        return isBlank(primary) ? fallback : primary;
    }

    private String defaultValue(String primary, String fallback) {
        return isBlank(primary) ? fallback : primary;
    }

    private LocalDateTime defaultTime(LocalDateTime primary, LocalDateTime fallback) {
        return primary != null ? primary : fallback;
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
