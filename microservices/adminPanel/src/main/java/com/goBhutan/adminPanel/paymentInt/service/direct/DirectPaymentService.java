package com.goBhutan.adminPanel.paymentInt.service.direct;

import com.goBhutan.adminPanel.paymentInt.dto.DirectPaymentAccountInquiryRequest;
import com.goBhutan.adminPanel.paymentInt.dto.DirectPaymentDebitRequest;
import com.goBhutan.adminPanel.paymentInt.dto.DirectPaymentInitiateRequest;
import com.goBhutan.adminPanel.paymentInt.dto.GatewayPaymentAccountInquiryResponse;
import com.goBhutan.adminPanel.paymentInt.dto.GatewayPaymentDebitResponse;
import com.goBhutan.adminPanel.paymentInt.dto.GatewayPaymentInitiateResponse;
import com.goBhutan.adminPanel.paymentInt.dto.PaymentStatusResponse;
import com.goBhutan.adminPanel.paymentInt.dto.ServicePaymentRequest;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import com.goBhutan.adminPanel.paymentInt.service.PaymentIntegrationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectPaymentService {

    private static final String PAYMENT_METHOD_DIRECT_GATEWAY = "DIRECT_GATEWAY";

    private final PaymentIntegrationService paymentService;
    private final List<DirectPaymentHandler> handlers;

    private Map<String, DirectPaymentHandler> handlerMap;

    @PostConstruct
    private void initHandlerMap() {
        handlerMap = handlers.stream()
                .collect(Collectors.toMap(h -> normalize(h.module()), Function.identity()));
    }

    public Map<String, Object> initiate(String module, DirectPaymentInitiateRequest request, String userId) {
        DirectPaymentHandler handler = handler(module);
        String referenceId = referenceId(request);

        ServicePaymentRequest paymentRequest = handler.buildPaymentRequest(referenceId, userId, request);
        GatewayPaymentInitiateResponse payment = paymentService.initiateGatewayServicePayment(
                paymentRequest,
                userId,
                request.getRemitterEmail());

        if (payment.getStatus() == PaymentStatus.PENDING) {
            handler.onPaymentPending(referenceId, userId, payment.getExpiresAt());
        }

        Map<String, Object> response = baseResponse(handler, referenceId);
        response.put("paymentRef", payment.getPaymentRef());
        response.put("providerTransactionId", payment.getProviderTransactionId());
        response.put("status", payment.getStatus());
        response.put("amount", payment.getAmount());
        response.put("currency", payment.getCurrency());
        response.put("checkoutUrl", payment.getCheckoutUrl());
        response.put("description", paymentRequest.getDescription());
        response.put("responseCode", payment.getResponseCode());
        response.put("responseDesc", payment.getResponseDesc());
        response.put("bankList", payment.getBankList());
        response.put("expiresAt", payment.getExpiresAt());
        return response;
    }

    public Map<String, Object> accountInquiry(String module, DirectPaymentAccountInquiryRequest request, String userId) {
        DirectPaymentHandler handler = handler(module);
        String referenceId = handler.ensurePaymentCanContinue(request.getPaymentRef(), userId);

        GatewayPaymentAccountInquiryResponse payment = paymentService.verifyGatewayServicePaymentAccount(
                request.getPaymentRef(),
                request.getRemitterBankId(),
                request.getRemitterAccNo(),
                userId);

        Map<String, Object> response = baseResponse(handler, referenceId);
        response.put("payment", payment);
        return response;
    }

    public Map<String, Object> debit(String module, DirectPaymentDebitRequest request, String userId) {
        DirectPaymentHandler handler = handler(module);
        String referenceId = handler.ensurePaymentCanDebit(request.getPaymentRef(), userId);

        GatewayPaymentDebitResponse payment = paymentService.submitGatewayServicePaymentOtp(
                request.getPaymentRef(),
                request.getOtp(),
                userId);

        Map<String, Object> response = baseResponse(handler, referenceId);
        response.put("payment", payment);
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            putConfirmation(response, handler, handler.confirmPaymentSuccess(request.getPaymentRef(), userId));
        }
        return response;
    }

    public Map<String, Object> status(String module, String paymentRef, String userId) {
        DirectPaymentHandler handler = handler(module);

        PaymentStatusResponse payment = paymentService.getGatewayServicePaymentStatus(paymentRef, userId);
        Map<String, Object> response = baseResponse(handler, null);
        response.put("payment", payment);
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            Object confirmation = handler.confirmPaymentSuccess(paymentRef, userId);
            putConfirmation(response, handler, confirmation);
        }
        return response;
    }

    public PaymentStatus statusOf(Map<String, Object> response) {
        Object status = response.get("status");
        if (status instanceof PaymentStatus paymentStatus) {
            return paymentStatus;
        }
        Object payment = response.get("payment");
        if (payment instanceof GatewayPaymentAccountInquiryResponse accountInquiry) {
            return accountInquiry.getStatus();
        }
        if (payment instanceof GatewayPaymentDebitResponse debit) {
            return debit.getStatus();
        }
        if (payment instanceof PaymentStatusResponse paymentStatus) {
            return paymentStatus.getStatus();
        }
        return PaymentStatus.PENDING;
    }

    private Map<String, Object> baseResponse(DirectPaymentHandler handler, String referenceId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("module", handler.module());
        response.put("paymentMethod", PAYMENT_METHOD_DIRECT_GATEWAY);
        response.put("referenceType", handler.referenceType());
        if (referenceId != null) {
            response.put("referenceId", referenceId);
            response.put(handler.referenceIdKey(), referenceId);
        }
        return response;
    }

    private void putConfirmation(Map<String, Object> response, DirectPaymentHandler handler, Object confirmation) {
        response.put(handler.confirmationKey(), confirmation);
        if (confirmation instanceof Map<?, ?> confirmationMap) {
            Object referenceId = confirmationMap.get(handler.referenceIdKey());
            if (referenceId != null && !response.containsKey(handler.referenceIdKey())) {
                response.put(handler.referenceIdKey(), referenceId);
                response.put("referenceId", referenceId);
            }
        }
    }

    private DirectPaymentHandler handler(String module) {
        if (module == null || module.trim().isEmpty()) {
            throw new RuntimeException("module is required");
        }
        DirectPaymentHandler handler = handlerMap.get(normalize(module));
        if (handler == null) {
            throw new RuntimeException("Unsupported direct payment module: " + module +
                    ". Available modules: " + handlerMap.keySet());
        }
        return handler;
    }

    private String referenceId(DirectPaymentInitiateRequest request) {
        if (request == null) {
            throw new RuntimeException("request is required");
        }
        String referenceId = firstNonBlank(request.getReferenceId(), request.getBookingRef());
        if (referenceId == null) {
            throw new RuntimeException("referenceId is required");
        }
        return referenceId;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        if (second != null && !second.trim().isEmpty()) {
            return second.trim();
        }
        return null;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}