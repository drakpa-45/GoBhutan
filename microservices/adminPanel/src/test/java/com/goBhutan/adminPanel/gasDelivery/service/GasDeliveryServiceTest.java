package com.goBhutan.adminPanel.gasDelivery.service;

import com.goBhutan.adminPanel.gasDelivery.dto.GasDeliveryAdminStatusRequest;
import com.goBhutan.adminPanel.gasDelivery.entity.GasConfigMaster;
import com.goBhutan.adminPanel.gasDelivery.entity.GasDeliveryDtls;
import com.goBhutan.adminPanel.gasDelivery.entity.GasDeliveryItemDtls;
import com.goBhutan.adminPanel.gasDelivery.enums.GasDeliveryStatus;
import com.goBhutan.adminPanel.gasDelivery.repository.GasConfigMasterRepository;
import com.goBhutan.adminPanel.gasDelivery.repository.GasDeliveryDtlsRepository;
import com.goBhutan.adminPanel.notification.dto.NotificationRequest;
import com.goBhutan.adminPanel.notification.enums.NotificationCategory;
import com.goBhutan.adminPanel.notification.enums.NotificationChannel;
import com.goBhutan.adminPanel.notification.event.NotificationAfterCommitPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GasDeliveryServiceTest {

    @Mock private GasDeliveryDtlsRepository deliveryRepository;
    @Mock private GasConfigMasterRepository gasConfigRepository;
    @Mock private ObjectProvider<NotificationAfterCommitPublisher> publisherProvider;
    @Mock private NotificationAfterCommitPublisher publisher;

    private GasDeliveryService service;

    @BeforeEach
    void setUp() {
        service = new GasDeliveryService(deliveryRepository, gasConfigRepository, publisherProvider);
    }

    @Test
    void dispatchNotifiesTheCustomerStoredOnTheDelivery() {
        GasDeliveryDtls delivery = pendingDelivery();
        GasDeliveryAdminStatusRequest request = statusRequest(GasDeliveryStatus.DISPATCHED);

        when(deliveryRepository.findWithLockById(42L)).thenReturn(Optional.of(delivery));
        when(gasConfigRepository.findByIdAndActiveTrue(7L))
                .thenReturn(Optional.of(delivery.getItems().get(0).getGasConfig()));
        when(deliveryRepository.save(delivery)).thenReturn(delivery);
        when(publisherProvider.getIfAvailable()).thenReturn(publisher);

        service.updateStatus(42L, request, "admin-keycloak-sub");

        ArgumentCaptor<NotificationRequest> notification =
                ArgumentCaptor.forClass(NotificationRequest.class);
        verify(publisher).sendAfterCommit(notification.capture());

        NotificationRequest sent = notification.getValue();
        assertEquals("customer-keycloak-sub", sent.getRecipientId());
        assertEquals(NotificationCategory.GAS_DELIVERY_UPDATE, sent.getCategory());
        assertEquals(NotificationChannel.PUSH_AND_IN_APP, sent.getChannel());
        assertEquals("GAS:GAS_DELIVERY_UPDATE:42:DISPATCHED", sent.getEventId());
        assertEquals("42", sent.getSourceEntityId());
        assertEquals("DISPATCHED", sent.getData().get("status"));
    }

    @Test
    void cancellationDoesNotPublishAnApprovalNotification() {
        GasDeliveryDtls delivery = pendingDelivery();
        GasDeliveryAdminStatusRequest request = statusRequest(GasDeliveryStatus.CANCELLED);

        when(deliveryRepository.findWithLockById(42L)).thenReturn(Optional.of(delivery));
        when(gasConfigRepository.findByIdAndActiveTrue(7L))
                .thenReturn(Optional.of(delivery.getItems().get(0).getGasConfig()));
        when(deliveryRepository.save(delivery)).thenReturn(delivery);

        service.updateStatus(42L, request, "admin-keycloak-sub");

        verify(publisherProvider, never()).getIfAvailable();
        verify(publisher, never()).sendAfterCommit(any());
    }

    @Test
    void dispatchStillSucceedsWhenNotificationsAreDisabled() {
        GasDeliveryDtls delivery = pendingDelivery();
        GasDeliveryAdminStatusRequest request = statusRequest(GasDeliveryStatus.DISPATCHED);

        when(deliveryRepository.findWithLockById(42L)).thenReturn(Optional.of(delivery));
        when(gasConfigRepository.findByIdAndActiveTrue(7L))
                .thenReturn(Optional.of(delivery.getItems().get(0).getGasConfig()));
        when(deliveryRepository.save(delivery)).thenReturn(delivery);
        when(publisherProvider.getIfAvailable()).thenReturn(null);

        assertEquals(
                GasDeliveryStatus.DISPATCHED,
                service.updateStatus(42L, request, "admin-keycloak-sub").status());
    }

    private GasDeliveryDtls pendingDelivery() {
        GasConfigMaster gasConfig = new GasConfigMaster();
        gasConfig.setId(7L);
        gasConfig.setGasType("LPG");
        gasConfig.setQuantity(10);

        GasDeliveryItemDtls item = new GasDeliveryItemDtls();
        item.setId(9L);
        item.setGasConfig(gasConfig);
        item.setGasType("LPG");
        item.setQuantity(2);

        GasDeliveryDtls delivery = new GasDeliveryDtls();
        delivery.setId(42L);
        delivery.setMobileNumber("17123456");
        delivery.setUserId("customer-keycloak-sub");
        delivery.setStatus(GasDeliveryStatus.PENDING);
        delivery.addItem(item);
        return delivery;
    }

    private GasDeliveryAdminStatusRequest statusRequest(GasDeliveryStatus status) {
        GasDeliveryAdminStatusRequest request = new GasDeliveryAdminStatusRequest();
        request.setStatus(status);
        request.setAdminRemarks("Approved by gas administrator");
        request.setItems(List.of(
                new GasDeliveryAdminStatusRequest.GasDeliveryAdminItemRequest(7L, 2)));
        return request;
    }
}
