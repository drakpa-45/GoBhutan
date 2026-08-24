package com.goBhutan.adminPanel.gasDelivery.service;

import com.goBhutan.adminPanel.gasDelivery.dto.GasDeliveryAdminStatusRequest;
import com.goBhutan.adminPanel.gasDelivery.dto.GasDeliveryCreateRequest;
import com.goBhutan.adminPanel.gasDelivery.dto.GasDeliveryResponse;
import com.goBhutan.adminPanel.gasDelivery.entity.GasConfigMaster;
import com.goBhutan.adminPanel.gasDelivery.entity.GasDeliveryDtls;
import com.goBhutan.adminPanel.gasDelivery.entity.GasDeliveryItemDtls;
import com.goBhutan.adminPanel.gasDelivery.enums.GasDeliveryStatus;
import com.goBhutan.adminPanel.gasDelivery.repository.GasConfigMasterRepository;
import com.goBhutan.adminPanel.gasDelivery.repository.GasDeliveryDtlsRepository;
import com.goBhutan.adminPanel.notification.dto.NotificationRequest;
import com.goBhutan.adminPanel.notification.enums.NotificationCategory;
import com.goBhutan.adminPanel.notification.enums.NotificationChannel;
import com.goBhutan.adminPanel.notification.enums.NotificationPriority;
import com.goBhutan.adminPanel.notification.event.NotificationAfterCommitPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GasDeliveryService {

    private final GasDeliveryDtlsRepository gasDeliveryDtlsRepository;
    private final GasConfigMasterRepository gasConfigMasterRepository;
    private final ObjectProvider<NotificationAfterCommitPublisher> notificationPublisherProvider;

    public GasDeliveryResponse create(GasDeliveryCreateRequest request, String userId) {
        validateDuplicateGasTypes(request);

        GasDeliveryDtls delivery = new GasDeliveryDtls();
        delivery.setMobileNumber(request.getMobileNumber().trim());
        delivery.setCidNumber(request.getCidNumber());
        delivery.setFullName(request.getFullName());
        delivery.setExpectedDeliveryTime(request.getExpectedDeliveryTime());
        delivery.setUserId(userId);
        delivery.setStatus(GasDeliveryStatus.PENDING);
        delivery.setCustomerRemarks(trimToNull(request.getCustomerRemarks()));

        request.getItems().forEach(itemRequest -> {
            GasConfigMaster gasConfig = gasConfigMasterRepository.findByIdAndActiveTrue(itemRequest.getGasConfigId())
                    .orElseThrow(() -> new RuntimeException("Active gas type not found"));

            Integer availableQuantity = gasConfig.getQuantity() == null ? 0 : gasConfig.getQuantity();
            if (availableQuantity < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for gas type: " + gasConfig.getGasType());
            }

            gasConfig.setQuantity(availableQuantity - itemRequest.getQuantity());

            GasDeliveryItemDtls item = new GasDeliveryItemDtls();
            item.setGasConfig(gasConfig);
            item.setGasType(gasConfig.getGasType());
            item.setQuantity(itemRequest.getQuantity());
            delivery.addItem(item);
        });

        return toResponse(gasDeliveryDtlsRepository.save(delivery));
    }

    public List<GasDeliveryResponse> getAll() {
        return gasDeliveryDtlsRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public GasDeliveryResponse updateStatus(Long id, GasDeliveryAdminStatusRequest request, String adminUserId) {
        GasDeliveryDtls delivery = gasDeliveryDtlsRepository.findWithLockById(id)
                .orElseThrow(() -> new RuntimeException("Gas delivery request not found"));

        if (delivery.getStatus() != GasDeliveryStatus.PENDING) {
            throw new RuntimeException("Only pending gas delivery requests can be updated");
        }

        if (request.getStatus() == GasDeliveryStatus.DISPATCHED) {
            updateDispatchQuantities(delivery, request);
        } else if (request.getStatus() == GasDeliveryStatus.CANCELLED) {
            revertReservedStock(delivery);
        } else {
            throw new RuntimeException("Status must be DISPATCHED or CANCELLED");
        }

        delivery.setStatus(request.getStatus());
        delivery.setAdminUserId(adminUserId);
        delivery.setAdminRemarks(trimToNull(request.getAdminRemarks()));

        GasDeliveryDtls saved = gasDeliveryDtlsRepository.save(delivery);
        if (saved.getStatus() == GasDeliveryStatus.DISPATCHED) {
            publishDeliveryUpdate(saved);
        }

        return toResponse(saved);
    }

    private void publishDeliveryUpdate(GasDeliveryDtls delivery) {
        NotificationAfterCommitPublisher publisher = notificationPublisherProvider.getIfAvailable();
        if (publisher == null) {
            return;
        }

        String deliveryId = delivery.getId().toString();
        NotificationRequest notification = NotificationRequest.builder()
                .eventId("GAS:GAS_DELIVERY_UPDATE:" + deliveryId + ":" + delivery.getStatus())
                .recipientId(delivery.getUserId())
                .title("Gas delivery approved")
                .body("Your gas delivery request has been approved and dispatched.")
                .category(NotificationCategory.GAS_DELIVERY_UPDATE)
                .channel(NotificationChannel.PUSH_AND_IN_APP)
                .sourceModule("GAS")
                .sourceEntityType("GAS_DELIVERY")
                .sourceEntityId(deliveryId)
                .actionType("OPEN_GAS_DELIVERY")
                .actionValue(deliveryId)
                .priority(NotificationPriority.NORMAL)
                .data(Map.of(
                        "deliveryId", deliveryId,
                        "status", delivery.getStatus().name()))
                .build();

        publisher.sendAfterCommit(notification);
    }

    private void updateDispatchQuantities(GasDeliveryDtls delivery, GasDeliveryAdminStatusRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("At least one gas item is required for dispatch");
        }

        validateDuplicateGasTypes(request);

        Map<Long, GasDeliveryItemDtls> existingItems = delivery.getItems().stream()
                .collect(Collectors.toMap(item -> item.getGasConfig().getId(), Function.identity()));

        if (request.getItems().size() != existingItems.size()) {
            throw new RuntimeException("Dispatch quantities must be provided for all requested gas items");
        }

        request.getItems().forEach(itemRequest -> {
            GasDeliveryItemDtls item = existingItems.get(itemRequest.getGasConfigId());
            if (item == null) {
                throw new RuntimeException("Gas item does not belong to this delivery request");
            }

            GasConfigMaster gasConfig = gasConfigMasterRepository.findByIdAndActiveTrue(itemRequest.getGasConfigId())
                    .orElseThrow(() -> new RuntimeException("Active gas type not found"));

            int currentQuantity = item.getQuantity() == null ? 0 : item.getQuantity();
            int newQuantity = itemRequest.getQuantity() == null ? 0 : itemRequest.getQuantity();
            int quantityDifference = newQuantity - currentQuantity;

            if (quantityDifference > 0) {
                subtractStock(gasConfig, quantityDifference);
            } else if (quantityDifference < 0) {
                addStock(gasConfig, Math.abs(quantityDifference));
            }

            item.setQuantity(newQuantity);
        });
    }

    private void revertReservedStock(GasDeliveryDtls delivery) {
        delivery.getItems().forEach(item -> {
            GasConfigMaster gasConfig = gasConfigMasterRepository.findByIdAndActiveTrue(item.getGasConfig().getId())
                    .orElseThrow(() -> new RuntimeException("Active gas type not found"));
            addStock(gasConfig, item.getQuantity() == null ? 0 : item.getQuantity());
        });
    }

    private void subtractStock(GasConfigMaster gasConfig, int quantity) {
        int availableQuantity = gasConfig.getQuantity() == null ? 0 : gasConfig.getQuantity();
        if (availableQuantity < quantity) {
            throw new RuntimeException("Insufficient stock for gas type: " + gasConfig.getGasType());
        }
        gasConfig.setQuantity(availableQuantity - quantity);
    }

    private void addStock(GasConfigMaster gasConfig, int quantity) {
        int availableQuantity = gasConfig.getQuantity() == null ? 0 : gasConfig.getQuantity();
        gasConfig.setQuantity(availableQuantity + quantity);
    }

    private void validateDuplicateGasTypes(GasDeliveryCreateRequest request) {
        Set<Long> gasConfigIds = new HashSet<>();
        request.getItems().forEach(item -> {
            if (!gasConfigIds.add(item.getGasConfigId())) {
                throw new RuntimeException("Duplicate gas type selected");
            }
        });
    }

    private void validateDuplicateGasTypes(GasDeliveryAdminStatusRequest request) {
        Set<Long> gasConfigIds = new HashSet<>();
        request.getItems().forEach(item -> {
            if (!gasConfigIds.add(item.getGasConfigId())) {
                throw new RuntimeException("Duplicate gas type selected");
            }
        });
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private GasDeliveryResponse toResponse(GasDeliveryDtls delivery) {
        return new GasDeliveryResponse(
                delivery.getId(),
                delivery.getMobileNumber(),
                delivery.getCidNumber(),
                delivery.getFullName(),
                delivery.getExpectedDeliveryTime(),
                delivery.getUserId(),
                delivery.getStatus(),
                delivery.getCustomerRemarks(),
                delivery.getAdminRemarks(),
                delivery.getItems().stream()
                        .map(item -> new GasDeliveryResponse.GasDeliveryItemResponse(
                                item.getId(),
                                item.getGasConfig().getId(),
                                item.getGasType(),
                                item.getQuantity()
                        ))
                        .toList(),
                delivery.getCreatedAt(),
                delivery.getAdminUserId(),
                delivery.getUpdatedAt()
        );
    }
}
