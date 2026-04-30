package com.goBhutan.adminPanel.paymentInt.service;

import com.goBhutan.adminPanel.paymentInt.config.BfsSecureProperties;
import com.goBhutan.adminPanel.paymentInt.dto.BfsGatewayResponse;
import com.goBhutan.adminPanel.paymentInt.dto.GatewayPaymentAccountInquiryResponse;
import com.goBhutan.adminPanel.paymentInt.dto.GatewayPaymentDebitResponse;
import com.goBhutan.adminPanel.paymentInt.dto.GatewayPaymentInitiateResponse;
import com.goBhutan.adminPanel.paymentInt.dto.PaymentStatusResponse;
import com.goBhutan.adminPanel.paymentInt.dto.TopupAccountInquiryRequest;
import com.goBhutan.adminPanel.paymentInt.dto.TopupAccountInquiryResponse;
import com.goBhutan.adminPanel.paymentInt.dto.TopupDebitRequest;
import com.goBhutan.adminPanel.paymentInt.dto.TopupDebitResponse;
import com.goBhutan.adminPanel.paymentInt.dto.TopupInitiateRequest;
import com.goBhutan.adminPanel.paymentInt.dto.TopupInitiateResponse;
import com.goBhutan.adminPanel.paymentInt.dto.ServicePaymentRequest;
import com.goBhutan.adminPanel.paymentInt.dto.WalletBalanceResponse;
import com.goBhutan.adminPanel.paymentInt.dto.WalletConfigResponse;
import com.goBhutan.adminPanel.paymentInt.dto.WalletLedgerItemResponse;
import com.goBhutan.adminPanel.paymentInt.dto.WalletPaymentResult;
import com.goBhutan.adminPanel.paymentInt.entity.PaymentTransaction;
import com.goBhutan.adminPanel.paymentInt.entity.PaymentWalletConfig;
import com.goBhutan.adminPanel.paymentInt.entity.WalletAccount;
import com.goBhutan.adminPanel.paymentInt.entity.WalletLedger;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentProvider;
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
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentIntegrationService {

    private static final int MAX_STATUS_CHECK_COUNT = 3;
    private static final long STATUS_POLL_DELAY_MINUTES = 6;
    private static final long STATUS_POLL_INTERVAL_SECONDS = 60;
    private static final Pattern REMITTER_EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@([A-Z0-9-]+\\.)+[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

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
        ServicePaymentRequest paymentRequest = new ServicePaymentRequest();
        paymentRequest.setAmount(req.getAmount());
        paymentRequest.setCurrency(req.getCurrency());
        paymentRequest.setDescription(req.getDescription());

        return toTopupInitiateResponse(initiateGatewayPayment(
                paymentRequest,
                userId,
                req.getRemitterEmail(),
                PaymentTransactionType.WALLET_TOPUP,
                "Wallet topup"));
    }

    public TopupAccountInquiryResponse verifyTopupAccount(TopupAccountInquiryRequest req, String userId) {
        return toTopupAccountInquiryResponse(verifyGatewayPaymentAccount(
                req.getTopupRef(),
                req.getRemitterBankId(),
                req.getRemitterAccNo(),
                userId,
                PaymentTransactionType.WALLET_TOPUP));
    }

    public TopupDebitResponse submitTopupOtp(TopupDebitRequest req, String userId) {
        return toTopupDebitResponse(submitGatewayPaymentOtp(
                req.getTopupRef(),
                req.getOtp(),
                userId,
                PaymentTransactionType.WALLET_TOPUP,
                true));
    }

    public GatewayPaymentInitiateResponse initiateGatewayServicePayment(
            ServicePaymentRequest req,
            String userId,
            String remitterEmail
    ) {
        return initiateGatewayPayment(
                req,
                userId,
                remitterEmail,
                PaymentTransactionType.SERVICE_PAYMENT,
                "Service payment");
    }

    public GatewayPaymentAccountInquiryResponse verifyGatewayServicePaymentAccount(
            String paymentRef,
            String remitterBankId,
            String remitterAccNo,
            String userId
    ) {
        return verifyGatewayPaymentAccount(
                paymentRef,
                remitterBankId,
                remitterAccNo,
                userId,
                PaymentTransactionType.SERVICE_PAYMENT);
    }

    public GatewayPaymentDebitResponse submitGatewayServicePaymentOtp(String paymentRef, String otp, String userId) {
        return submitGatewayPaymentOtp(
                paymentRef,
                otp,
                userId,
                PaymentTransactionType.SERVICE_PAYMENT,
                false);
    }

    private GatewayPaymentInitiateResponse initiateGatewayPayment(
            ServicePaymentRequest req,
            String userId,
            String remitterEmail,
            PaymentTransactionType transactionType,
            String defaultDescription
    ) {
        validateAmount(req.getAmount());
        String normalizedRemitterEmail = validateRemitterEmail(remitterEmail);

        String referenceType = null;
        String referenceId = null;
        if (transactionType == PaymentTransactionType.SERVICE_PAYMENT) {
            if (isBlank(req.getReferenceType())) {
                throw new RuntimeException("referenceType is required");
            }
            if (isBlank(req.getReferenceId())) {
                throw new RuntimeException("referenceId is required");
            }

            referenceType = req.getReferenceType().trim();
            referenceId = req.getReferenceId().trim();
            ensureServicePaymentNotAlreadyInitiated(userId, referenceType, referenceId);
        }

        String currency = defaultCurrency(req.getCurrency());
        PaymentWalletConfig config = buildRuntimeConfig("SYSTEM");

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentRef(UUID.randomUUID().toString());
        transaction.setGatewayOrderNo(UUID.randomUUID().toString().replace("-", ""));
        transaction.setProvider(config.getProvider());
        transaction.setTransactionType(transactionType);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setAmount(req.getAmount().setScale(2, RoundingMode.HALF_UP));
        transaction.setCurrency(currency);
        transaction.setDescription(safeDescription(defaultMessage(req.getDescription(), defaultDescription)));
        transaction.setRemitterEmail(normalizedRemitterEmail);
        transaction.setUserId(userId);
        transaction.setServiceName(trimToNull(req.getServiceName()));
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        PaymentTransaction saved = transactionWriter.saveNew(transaction);

        try {
            BfsGatewayResponse bfsResponse = walletGatewayClient.initiatePayment(config, saved);

            saved.setProviderTransactionId(bfsResponse.getProviderTransactionId());
            saved.setProviderResponseCode(bfsResponse.getResponseCode());
            saved.setProviderMessage(defaultMessage(bfsResponse.getResponseDesc(), "Authorization request accepted"));
            saved.setRawArRequest(bfsResponse.getRawRequest());
            saved.setRawRcResponse(bfsResponse.getRawResponse());
            transitionFromGatewayCode(saved, bfsResponse.getResponseCode());
            saved = transactionWriter.save(saved);

            return toGatewayInitiateResponse(saved, bfsResponse);
        } catch (RuntimeException ex) {
            saved.setStatus(PaymentStatus.FAILED);
            saved.setProviderMessage(defaultMessage(ex.getMessage(),
                    "Unable to initiate BFS " + gatewayPaymentLabel(transactionType)));
            saved = transactionWriter.save(saved);

            return toFailedGatewayInitiateResponse(saved);
        }
    }

    private GatewayPaymentAccountInquiryResponse verifyGatewayPaymentAccount(
            String paymentRef,
            String remitterBankId,
            String remitterAccNo,
            String userId,
            PaymentTransactionType transactionType
    ) {
        if (isBlank(paymentRef)) {
            throw new RuntimeException("paymentRef is required");
        }
        if (isBlank(remitterBankId)) {
            throw new RuntimeException("remitterBankId is required");
        }
        if (isBlank(remitterAccNo)) {
            throw new RuntimeException("remitterAccNo is required");
        }

        PaymentTransaction transaction = getOwnedGatewayPayment(paymentRef, userId, transactionType);
        ensureInteractiveStepAllowed(transaction);
        ensurePendingPayment(transaction);

        if (isBlank(transaction.getProviderTransactionId())) {
            throw new RuntimeException(capitalize(gatewayPaymentLabel(transactionType))
                    + " authorization must be initiated first");
        }

        String bankId = remitterBankId.trim().toUpperCase();
        validateBankSelection(transaction, bankId);

        PaymentWalletConfig config = buildRuntimeConfig("SYSTEM");
        BfsGatewayResponse bfsResponse = walletGatewayClient.verifyAccount(config, transaction, bankId,
                remitterAccNo.trim());

        transaction.setRemitterBankId(bankId);
        transaction.setRemitterAccNoMasked(maskAccount(remitterAccNo.trim()));
        transaction.setProviderResponseCode(bfsResponse.getResponseCode());
        transaction.setProviderMessage(defaultMessage(bfsResponse.getResponseDesc(), "Account inquiry completed"));
        transaction.setRawAeRequest(bfsResponse.getRawRequest());
        transaction.setRawEcResponse(bfsResponse.getRawResponse());
        transitionFromGatewayCode(transaction, bfsResponse.getResponseCode());
        transactionRepository.save(transaction);

        return GatewayPaymentAccountInquiryResponse.builder()
                .paymentRef(transaction.getPaymentRef())
                .providerTransactionId(transaction.getProviderTransactionId())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .responseCode(bfsResponse.getResponseCode())
                .responseDesc(bfsResponse.getResponseDesc())
                .otpRequired("00".equals(bfsResponse.getResponseCode()))
                .remitterBankId(bankId)
                .remitterAccNoMasked(transaction.getRemitterAccNoMasked())
                .build();
    }

    private GatewayPaymentDebitResponse submitGatewayPaymentOtp(
            String paymentRef,
            String otp,
            String userId,
            PaymentTransactionType transactionType,
            boolean creditTopupWallet
    ) {
        if (isBlank(paymentRef)) {
            throw new RuntimeException("paymentRef is required");
        }
        if (!isValidOtp(otp)) {
            throw new RuntimeException("otp must be exactly 6 digits");
        }

        PaymentTransaction transaction = getOwnedGatewayPayment(paymentRef, userId, transactionType);
        ensureInteractiveStepAllowed(transaction);
        ensurePendingPayment(transaction);

        if (isBlank(transaction.getProviderTransactionId())) {
            throw new RuntimeException(capitalize(gatewayPaymentLabel(transactionType))
                    + " authorization must be initiated first");
        }
        if (isBlank(transaction.getRemitterBankId())) {
            throw new RuntimeException("Account inquiry must be completed before OTP submission");
        }

        PaymentWalletConfig config = buildRuntimeConfig("SYSTEM");
        BfsGatewayResponse bfsResponse = walletGatewayClient.submitOtp(config, transaction, otp.trim());

        transaction.setDebitRequestedAt(LocalDateTime.now());
        applyDebitResult(transaction, bfsResponse, creditTopupWallet);
        transaction.setRawDrRequest(bfsResponse.getRawRequest());
        transaction.setRawAcResponse(bfsResponse.getRawResponse());
        transactionRepository.save(transaction);

        return GatewayPaymentDebitResponse.builder()
                .paymentRef(transaction.getPaymentRef())
                .providerTransactionId(transaction.getProviderTransactionId())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .responseCode(transaction.getProviderResponseCode())
                .responseDesc(transaction.getProviderMessage())
                .debitAuthCode(transaction.getDebitAuthCode())
                .debitAuthNo(transaction.getDebitAuthNo())
                .remitterName(transaction.getRemitterName())
                .remitterBankId(transaction.getRemitterBankId())
                .build();
    }

    private void ensureServicePaymentNotAlreadyInitiated(String userId, String referenceType, String referenceId) {
        transactionRepository
                .findFirstByUserIdAndReferenceTypeAndReferenceIdAndTransactionTypeAndStatusInOrderByCreatedAtDesc(
                        userId,
                        referenceType,
                        referenceId,
                        PaymentTransactionType.SERVICE_PAYMENT,
                        List.of(PaymentStatus.PENDING, PaymentStatus.SUCCESS)
                )
                .ifPresent(existing -> {
                    if (existing.getStatus() == PaymentStatus.PENDING && expireIfNeeded(existing)) {
                        return;
                    }
                    throw new RuntimeException("Payment already initiated for this reference");
                });
    }

    private GatewayPaymentInitiateResponse toGatewayInitiateResponse(
            PaymentTransaction transaction,
            BfsGatewayResponse bfsResponse
    ) {
        return GatewayPaymentInitiateResponse.builder()
                .paymentRef(transaction.getPaymentRef())
                .providerTransactionId(transaction.getProviderTransactionId())
                .checkoutUrl(null)
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .responseCode(bfsResponse.getResponseCode())
                .responseDesc(bfsResponse.getResponseDesc())
                .bankList(bfsResponse.safeBankList())
                .expiresAt(transaction.getExpiresAt())
                .build();
    }

    private GatewayPaymentInitiateResponse toFailedGatewayInitiateResponse(PaymentTransaction transaction) {
        return GatewayPaymentInitiateResponse.builder()
                .paymentRef(transaction.getPaymentRef())
                .providerTransactionId(transaction.getProviderTransactionId())
                .checkoutUrl(null)
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .responseCode(transaction.getProviderResponseCode())
                .responseDesc(transaction.getProviderMessage())
                .bankList(Collections.emptyList())
                .expiresAt(transaction.getExpiresAt())
                .build();
    }

    private TopupInitiateResponse toTopupInitiateResponse(GatewayPaymentInitiateResponse response) {
        return TopupInitiateResponse.builder()
                .topupRef(response.getPaymentRef())
                .providerTransactionId(response.getProviderTransactionId())
                .checkoutUrl(response.getCheckoutUrl())
                .status(response.getStatus())
                .responseCode(response.getResponseCode())
                .responseDesc(response.getResponseDesc())
                .bankList(response.getBankList())
                .expiresAt(response.getExpiresAt())
                .build();
    }

    private TopupAccountInquiryResponse toTopupAccountInquiryResponse(GatewayPaymentAccountInquiryResponse response) {
        return TopupAccountInquiryResponse.builder()
                .topupRef(response.getPaymentRef())
                .providerTransactionId(response.getProviderTransactionId())
                .status(response.getStatus())
                .responseCode(response.getResponseCode())
                .responseDesc(response.getResponseDesc())
                .otpRequired(response.isOtpRequired())
                .remitterBankId(response.getRemitterBankId())
                .remitterAccNoMasked(response.getRemitterAccNoMasked())
                .build();
    }

    private TopupDebitResponse toTopupDebitResponse(GatewayPaymentDebitResponse response) {
        return TopupDebitResponse.builder()
                .topupRef(response.getPaymentRef())
                .providerTransactionId(response.getProviderTransactionId())
                .status(response.getStatus())
                .responseCode(response.getResponseCode())
                .responseDesc(response.getResponseDesc())
                .debitAuthCode(response.getDebitAuthCode())
                .debitAuthNo(response.getDebitAuthNo())
                .remitterName(response.getRemitterName())
                .remitterBankId(response.getRemitterBankId())
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

    public WalletPaymentResult payWithWallet(ServicePaymentRequest req, String userId) {
        validateAmount(req.getAmount());

        if (isBlank(req.getReferenceType())) {
            throw new RuntimeException("referenceType is required");
        }
        if (isBlank(req.getReferenceId())) {
            throw new RuntimeException("referenceId is required");
        }

        String referenceType = req.getReferenceType().trim();
        String referenceId = req.getReferenceId().trim();

        String currency = defaultCurrency(req.getCurrency());
        WalletAccount wallet = walletAccountRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> createNewWallet(userId, currency));

        if (!"ACTIVE".equalsIgnoreCase(wallet.getStatus())) {
            throw new RuntimeException("Wallet is not active");
        }

        if (transactionRepository.findFirstByUserIdAndReferenceTypeAndReferenceIdAndTransactionTypeAndStatus(
                userId,
                referenceType,
                referenceId,
                PaymentTransactionType.SERVICE_PAYMENT,
                PaymentStatus.SUCCESS
        ).isPresent()) {
            throw new RuntimeException("Payment already completed for this reference");
        }

        BigDecimal amount = req.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal before = wallet.getBalance();

        if (before.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }

        BigDecimal after = before.subtract(amount);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentRef(UUID.randomUUID().toString());
        transaction.setProvider(PaymentProvider.INTERNAL_WALLET);
        transaction.setTransactionType(PaymentTransactionType.SERVICE_PAYMENT);
        transaction.setStatus(PaymentStatus.SUCCESS);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setDescription(safeDescription(req.getDescription()));
        transaction.setUserId(userId);
        transaction.setServiceName(trimToNull(req.getServiceName()));
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setProviderMessage("Wallet payment completed");
        transactionRepository.save(transaction);

        wallet.setBalance(after);
        walletAccountRepository.save(wallet);

        WalletLedger ledger = new WalletLedger();
        ledger.setUserId(userId);
        ledger.setType(WalletLedgerType.DEBIT);
        ledger.setAmount(amount);
        ledger.setBalanceBefore(before);
        ledger.setBalanceAfter(after);
        ledger.setReferenceType(referenceType);
        ledger.setReferenceId(referenceId);
        ledger.setRemarks(defaultMessage(req.getDescription(), "Wallet payment"));
        walletLedgerRepository.save(ledger);

        return WalletPaymentResult.builder()
                .paymentRef(transaction.getPaymentRef())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .balanceBefore(before)
                .balanceAfter(after)
                .serviceName(transaction.getServiceName())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .message(transaction.getProviderMessage())
                .build();
    }

    public WalletPaymentResult refundToWallet(
            String originalPaymentRef,
            BigDecimal amount,
            String serviceName,
            String referenceType,
            String referenceId,
            String description,
            String userId
    ) {
        validateAmount(amount);

        PaymentTransaction original = transactionRepository.findByPaymentRefAndUserId(originalPaymentRef, userId)
                .orElseThrow(() -> new RuntimeException("Original payment transaction not found"));

        if (original.getTransactionType() != PaymentTransactionType.SERVICE_PAYMENT
                || original.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Original transaction is not a successful service payment");
        }

        WalletAccount wallet = walletAccountRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> createNewWallet(userId, original.getCurrency()));

        BigDecimal existingRefundAmount = transactionRepository.sumAmountByUserIdAndParentPaymentRefAndTransactionTypeAndStatus(
                userId,
                originalPaymentRef,
                PaymentTransactionType.SERVICE_REFUND,
                PaymentStatus.SUCCESS
        );

        BigDecimal refundAmount = amount.setScale(2, RoundingMode.HALF_UP);
        if (existingRefundAmount.add(refundAmount).compareTo(original.getAmount()) > 0) {
            throw new RuntimeException("Refund amount exceeds original payment");
        }

        BigDecimal before = wallet.getBalance();
        BigDecimal after = before.add(refundAmount);

        PaymentTransaction refundTxn = new PaymentTransaction();
        refundTxn.setPaymentRef(UUID.randomUUID().toString());
        refundTxn.setProvider(PaymentProvider.INTERNAL_WALLET);
        refundTxn.setTransactionType(PaymentTransactionType.SERVICE_REFUND);
        refundTxn.setStatus(PaymentStatus.SUCCESS);
        refundTxn.setAmount(refundAmount);
        refundTxn.setCurrency(original.getCurrency());
        refundTxn.setDescription(safeDescription(description));
        refundTxn.setUserId(userId);
        refundTxn.setServiceName(trimToNull(serviceName));
        refundTxn.setReferenceType(referenceType);
        refundTxn.setReferenceId(referenceId);
        refundTxn.setParentPaymentRef(originalPaymentRef);
        refundTxn.setProviderMessage("Wallet refund completed");
        transactionRepository.save(refundTxn);

        wallet.setBalance(after);
        walletAccountRepository.save(wallet);

        WalletLedger ledger = new WalletLedger();
        ledger.setUserId(userId);
        ledger.setType(WalletLedgerType.CREDIT);
        ledger.setAmount(refundAmount);
        ledger.setBalanceBefore(before);
        ledger.setBalanceAfter(after);
        ledger.setReferenceType(referenceType);
        ledger.setReferenceId(referenceId);
        ledger.setRemarks(defaultMessage(description, "Wallet refund"));
        walletLedgerRepository.save(ledger);

        return WalletPaymentResult.builder()
                .paymentRef(refundTxn.getPaymentRef())
                .status(refundTxn.getStatus())
                .amount(refundTxn.getAmount())
                .currency(refundTxn.getCurrency())
                .balanceBefore(before)
                .balanceAfter(after)
                .serviceName(refundTxn.getServiceName())
                .referenceType(refundTxn.getReferenceType())
                .referenceId(refundTxn.getReferenceId())
                .message(refundTxn.getProviderMessage())
                .build();
    }

    public BigDecimal getSuccessfulServicePaymentAmount(String paymentRef, String userId) {
        PaymentTransaction original = transactionRepository.findByPaymentRefAndUserId(paymentRef, userId)
                .orElseThrow(() -> new RuntimeException("Original payment transaction not found"));

        if (original.getTransactionType() != PaymentTransactionType.SERVICE_PAYMENT
                || original.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Original transaction is not a successful service payment");
        }

        return original.getAmount();
    }

    public String getPendingGatewayServicePaymentReferenceId(String paymentRef, String userId, String expectedReferenceType) {
        PaymentTransaction transaction = getOwnedGatewayServicePayment(paymentRef, userId);
        ensureServicePaymentReferenceType(transaction, expectedReferenceType);
        ensureInteractiveStepAllowed(transaction);
        ensurePendingPayment(transaction);
        return transaction.getReferenceId();
    }

    public String getSuccessfulGatewayServicePaymentReferenceId(String paymentRef, String userId, String expectedReferenceType) {
        PaymentTransaction transaction = getOwnedGatewayServicePayment(paymentRef, userId);
        ensureServicePaymentReferenceType(transaction, expectedReferenceType);
        ensureSuccessfulPayment(transaction);
        return transaction.getReferenceId();
    }

    public BigDecimal getSuccessfulGatewayServicePaymentAmount(String paymentRef, String userId, String expectedReferenceType) {
        PaymentTransaction transaction = getOwnedGatewayServicePayment(paymentRef, userId);
        ensureServicePaymentReferenceType(transaction, expectedReferenceType);
        ensureSuccessfulPayment(transaction);
        return transaction.getAmount();
    }

    public String getSuccessfulGatewayFundingSummary(String paymentRef, String userId, String expectedReferenceType) {
        PaymentTransaction transaction = getOwnedGatewayServicePayment(paymentRef, userId);
        ensureServicePaymentReferenceType(transaction, expectedReferenceType);
        ensureSuccessfulPayment(transaction);

        StringBuilder summary = new StringBuilder("gateway payment");
        appendSummaryPart(summary, "bank", transaction.getRemitterBankId());
        appendSummaryPart(summary, "account", transaction.getRemitterAccNoMasked());
        appendSummaryPart(summary, "remitter", transaction.getRemitterName());
        appendSummaryPart(summary, "paymentRef", transaction.getPaymentRef());
        return summary.toString();
    }

    public BigDecimal getSuccessfulSettlementAmount(String originalPaymentRef, String referenceType) {
        if (isBlank(referenceType)) {
            throw new RuntimeException("referenceType is required");
        }
        return transactionRepository.sumAmountByParentPaymentRefAndReferenceTypeAndTransactionTypeAndStatus(
                originalPaymentRef,
                referenceType.trim(),
                PaymentTransactionType.SERVICE_SETTLEMENT,
                PaymentStatus.SUCCESS
        );
    }

    public WalletPaymentResult creditServiceSettlement(
            String originalPaymentRef,
            BigDecimal amount,
            String serviceName,
            String referenceType,
            String referenceId,
            String description,
            String recipientUserId
    ) {
        validateAmount(amount);

        if (isBlank(recipientUserId)) {
            throw new RuntimeException("recipientUserId is required");
        }

        PaymentTransaction original = transactionRepository.findByPaymentRef(originalPaymentRef)
                .orElseThrow(() -> new RuntimeException("Original payment transaction not found"));

        if (original.getTransactionType() != PaymentTransactionType.SERVICE_PAYMENT
                || original.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Original transaction is not a successful service payment");
        }

        if (transactionRepository.findFirstByUserIdAndParentPaymentRefAndReferenceTypeAndTransactionTypeAndStatus(
                recipientUserId,
                originalPaymentRef,
                referenceType,
                PaymentTransactionType.SERVICE_SETTLEMENT,
                PaymentStatus.SUCCESS
        ).isPresent()) {
            throw new RuntimeException("Settlement already completed for this payment");
        }

        WalletAccount wallet = walletAccountRepository.findByUserIdForUpdate(recipientUserId)
                .orElseGet(() -> createNewWallet(recipientUserId, original.getCurrency()));

        BigDecimal settlementAmount = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal before = wallet.getBalance();
        BigDecimal after = before.add(settlementAmount);

        PaymentTransaction settlementTxn = new PaymentTransaction();
        settlementTxn.setPaymentRef(UUID.randomUUID().toString());
        settlementTxn.setProvider(PaymentProvider.INTERNAL_WALLET);
        settlementTxn.setTransactionType(PaymentTransactionType.SERVICE_SETTLEMENT);
        settlementTxn.setStatus(PaymentStatus.SUCCESS);
        settlementTxn.setAmount(settlementAmount);
        settlementTxn.setCurrency(original.getCurrency());
        settlementTxn.setDescription(safeDescription(description));
        settlementTxn.setUserId(recipientUserId);
        settlementTxn.setServiceName(trimToNull(serviceName));
        settlementTxn.setReferenceType(referenceType);
        settlementTxn.setReferenceId(referenceId);
        settlementTxn.setParentPaymentRef(originalPaymentRef);
        settlementTxn.setProviderMessage("Wallet settlement credited");
        transactionRepository.save(settlementTxn);

        wallet.setBalance(after);
        walletAccountRepository.save(wallet);

        WalletLedger ledger = new WalletLedger();
        ledger.setUserId(recipientUserId);
        ledger.setType(WalletLedgerType.CREDIT);
        ledger.setAmount(settlementAmount);
        ledger.setBalanceBefore(before);
        ledger.setBalanceAfter(after);
        ledger.setReferenceType(referenceType);
        ledger.setReferenceId(referenceId);
        ledger.setRemarks(defaultMessage(description, "Wallet settlement credit"));
        walletLedgerRepository.save(ledger);

        return WalletPaymentResult.builder()
                .paymentRef(settlementTxn.getPaymentRef())
                .status(settlementTxn.getStatus())
                .amount(settlementTxn.getAmount())
                .currency(settlementTxn.getCurrency())
                .balanceBefore(before)
                .balanceAfter(after)
                .serviceName(settlementTxn.getServiceName())
                .referenceType(settlementTxn.getReferenceType())
                .referenceId(settlementTxn.getReferenceId())
                .message(settlementTxn.getProviderMessage())
                .build();
    }

    public WalletPaymentResult reverseServiceSettlement(
            String originalPaymentRef,
            BigDecimal amount,
            String serviceName,
            String referenceType,
            String referenceId,
            String description,
            String recipientUserId
    ) {
        validateAmount(amount);

        if (isBlank(recipientUserId)) {
            throw new RuntimeException("recipientUserId is required");
        }

        PaymentTransaction settlementTxn = transactionRepository
                .findFirstByUserIdAndParentPaymentRefAndReferenceTypeAndTransactionTypeAndStatus(
                        recipientUserId,
                        originalPaymentRef,
                        referenceType,
                        PaymentTransactionType.SERVICE_SETTLEMENT,
                        PaymentStatus.SUCCESS
                )
                .orElseThrow(() -> new RuntimeException("Settlement transaction not found"));

        BigDecimal reversalAmount = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal existingReversalAmount = transactionRepository.sumAmountByUserIdAndParentPaymentRefAndTransactionTypeAndStatus(
                recipientUserId,
                settlementTxn.getPaymentRef(),
                PaymentTransactionType.SERVICE_SETTLEMENT_REVERSAL,
                PaymentStatus.SUCCESS
        );

        if (existingReversalAmount.add(reversalAmount).compareTo(settlementTxn.getAmount()) > 0) {
            throw new RuntimeException("Settlement reversal amount exceeds credited settlement");
        }

        WalletAccount wallet = walletAccountRepository.findByUserIdForUpdate(recipientUserId)
                .orElseGet(() -> createNewWallet(recipientUserId, settlementTxn.getCurrency()));

        BigDecimal before = wallet.getBalance();
        if (before.compareTo(reversalAmount) < 0) {
            throw new RuntimeException("Owner wallet has insufficient balance for refund reversal");
        }
        BigDecimal after = before.subtract(reversalAmount);

        PaymentTransaction reversalTxn = new PaymentTransaction();
        reversalTxn.setPaymentRef(UUID.randomUUID().toString());
        reversalTxn.setProvider(PaymentProvider.INTERNAL_WALLET);
        reversalTxn.setTransactionType(PaymentTransactionType.SERVICE_SETTLEMENT_REVERSAL);
        reversalTxn.setStatus(PaymentStatus.SUCCESS);
        reversalTxn.setAmount(reversalAmount);
        reversalTxn.setCurrency(settlementTxn.getCurrency());
        reversalTxn.setDescription(safeDescription(description));
        reversalTxn.setUserId(recipientUserId);
        reversalTxn.setServiceName(trimToNull(serviceName));
        reversalTxn.setReferenceType(referenceType);
        reversalTxn.setReferenceId(referenceId);
        reversalTxn.setParentPaymentRef(settlementTxn.getPaymentRef());
        reversalTxn.setProviderMessage("Wallet settlement reversed");
        transactionRepository.save(reversalTxn);

        wallet.setBalance(after);
        walletAccountRepository.save(wallet);

        WalletLedger ledger = new WalletLedger();
        ledger.setUserId(recipientUserId);
        ledger.setType(WalletLedgerType.DEBIT);
        ledger.setAmount(reversalAmount);
        ledger.setBalanceBefore(before);
        ledger.setBalanceAfter(after);
        ledger.setReferenceType(referenceType);
        ledger.setReferenceId(referenceId);
        ledger.setRemarks(defaultMessage(description, "Wallet settlement reversal"));
        walletLedgerRepository.save(ledger);

        return WalletPaymentResult.builder()
                .paymentRef(reversalTxn.getPaymentRef())
                .status(reversalTxn.getStatus())
                .amount(reversalTxn.getAmount())
                .currency(reversalTxn.getCurrency())
                .balanceBefore(before)
                .balanceAfter(after)
                .serviceName(reversalTxn.getServiceName())
                .referenceType(reversalTxn.getReferenceType())
                .referenceId(reversalTxn.getReferenceId())
                .message(reversalTxn.getProviderMessage())
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
        return getGatewayPaymentStatus(paymentRef, userId, PaymentTransactionType.WALLET_TOPUP, true);
    }

    public PaymentStatusResponse getGatewayServicePaymentStatus(String paymentRef, String userId) {
        return getGatewayPaymentStatus(paymentRef, userId, PaymentTransactionType.SERVICE_PAYMENT, false);
    }

    private PaymentStatusResponse getGatewayPaymentStatus(
            String paymentRef,
            String userId,
            PaymentTransactionType transactionType,
            boolean creditTopupWallet
    ) {
        PaymentTransaction transaction = getOwnedGatewayPayment(paymentRef, userId, transactionType);

        if (transaction.getStatus() == PaymentStatus.PENDING && transaction.getDebitRequestedAt() != null) {
            if (shouldPollStatus(transaction)) {
                pollGatewayStatus(transaction, creditTopupWallet);
            }
            return toPaymentStatus(transaction);
        }

        expireIfNeeded(transaction);
        return toPaymentStatus(transaction);
    }

    private void pollGatewayStatus(PaymentTransaction transaction, boolean creditTopupWallet) {
        PaymentWalletConfig config = buildRuntimeConfig("SYSTEM");
        BfsGatewayResponse bfsResponse = walletGatewayClient.checkStatus(config, transaction);

        transaction.setStatusCheckCount(statusCheckCount(transaction) + 1);
        transaction.setLastStatusCheckedAt(LocalDateTime.now());
        transaction.setRawAsRequest(bfsResponse.getRawRequest());
        transaction.setRawAsResponse(bfsResponse.getRawResponse());
        applyDebitResult(transaction, bfsResponse, creditTopupWallet);

        if (transaction.getStatus() == PaymentStatus.PENDING
                && statusCheckCount(transaction) >= MAX_STATUS_CHECK_COUNT) {
            transaction
                    .setProviderMessage("No final BFS response after 3 status checks; manual reconciliation required");
        }
        transactionRepository.save(transaction);
    }

    private boolean shouldPollStatus(PaymentTransaction transaction) {
        LocalDateTime now = LocalDateTime.now();
        return transaction.getDebitRequestedAt() != null
                && transaction.getWalletCreditedAt() == null
                && statusCheckCount(transaction) < MAX_STATUS_CHECK_COUNT
                && now.isAfter(transaction.getDebitRequestedAt().plusMinutes(STATUS_POLL_DELAY_MINUTES))
                && isStatusPollIntervalElapsed(transaction, now);
    }

    private boolean isStatusPollIntervalElapsed(PaymentTransaction transaction, LocalDateTime now) {
        return transaction.getLastStatusCheckedAt() == null
                || !now.isBefore(transaction.getLastStatusCheckedAt().plusSeconds(STATUS_POLL_INTERVAL_SECONDS));
    }

    private int statusCheckCount(PaymentTransaction transaction) {
        return transaction.getStatusCheckCount() == null ? 0 : transaction.getStatusCheckCount();
    }

    private void applyDebitResult(
            PaymentTransaction transaction,
            BfsGatewayResponse bfsResponse,
            boolean creditTopupWallet
    ) {
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
            if (creditTopupWallet && transaction.getWalletCreditedAt() == null) {
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

    private PaymentTransaction getOwnedGatewayServicePayment(String paymentRef, String userId) {
        return getOwnedGatewayPayment(paymentRef, userId, PaymentTransactionType.SERVICE_PAYMENT);
    }

    private PaymentTransaction getOwnedGatewayPayment(
            String paymentRef,
            String userId,
            PaymentTransactionType transactionType
    ) {
        PaymentTransaction transaction = transactionRepository.findByPaymentRef(paymentRef)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found"));

        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized payment access");
        }
        if (transaction.getTransactionType() != transactionType
                || transaction.getProvider() == PaymentProvider.INTERNAL_WALLET) {
            throw new RuntimeException("Unsupported payment transaction");
        }
        return transaction;
    }

    private void ensurePendingPayment(PaymentTransaction transaction) {
        if (transaction.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not pending");
        }
    }

    private void ensureSuccessfulPayment(PaymentTransaction transaction) {
        if (transaction.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment is not successful");
        }
    }

    private void ensureServicePaymentReferenceType(PaymentTransaction transaction, String expectedReferenceType) {
        if (isBlank(expectedReferenceType)) {
            throw new RuntimeException("referenceType is required");
        }
        if (!expectedReferenceType.trim().equals(transaction.getReferenceType())) {
            throw new RuntimeException("Payment reference type mismatch");
        }
    }

    private void ensureInteractiveStepAllowed(PaymentTransaction transaction) {
        if (expireIfNeeded(transaction)) {
            throw new RuntimeException("Payment session expired");
        }
    }

    private boolean expireIfNeeded(PaymentTransaction transaction) {
        if (transaction.getExpiresAt() != null
                && transaction.getExpiresAt().isBefore(LocalDateTime.now())
                && transaction.getStatus() == PaymentStatus.PENDING) {
            transaction.setStatus(PaymentStatus.EXPIRED);
            transaction.setProviderMessage("Payment session expired");
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

    private String validateRemitterEmail(String remitterEmail) {
        String normalized = trimToNull(remitterEmail);
        if (normalized == null) {
            throw new RuntimeException("remitterEmail is required");
        }
        if (!REMITTER_EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new RuntimeException("remitterEmail must be a valid email address");
        }
        return normalized;
    }

    private void validateBankSelection(PaymentTransaction transaction, String bankId) {
        Set<String> offeredBanks = extractOfferedBankIds(transaction);
        if (!offeredBanks.isEmpty() && !offeredBanks.contains(bankId)) {
            throw new RuntimeException("Selected remitter bank was not offered for this payment session");
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

    private String gatewayPaymentLabel(PaymentTransactionType transactionType) {
        return transactionType == PaymentTransactionType.WALLET_TOPUP ? "topup" : "service payment";
    }

    private String capitalize(String value) {
        if (isBlank(value)) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private LocalDateTime defaultTime(LocalDateTime primary, LocalDateTime fallback) {
        return primary != null ? primary : fallback;
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private void appendSummaryPart(StringBuilder summary, String label, String value) {
        if (!isBlank(value)) {
            summary.append(", ").append(label).append("=").append(value.trim());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
