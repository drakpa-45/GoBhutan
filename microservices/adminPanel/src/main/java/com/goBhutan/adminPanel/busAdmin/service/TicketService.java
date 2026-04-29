package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.BusTicketResponse;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import com.goBhutan.adminPanel.common.entity.AppUser;
import com.goBhutan.adminPanel.common.service.AppUserService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TicketService {
    private static final String PAYMENT_METHOD_CASH = "CASH";

    private final SeatBookingRepository bookingRepo;
    private final AppUserService appUserService;

    public BusTicketResponse getTicketDetails(Long bookingId, String userId) {
        SeatBooking booking = getAuthorizedBookedTicket(bookingId, userId);

        return toTicketResponse(booking);
    }

    public BusTicketResponse getCashTicketDetailsForAdmin(Long bookingId, String adminUserId) {
        SeatBooking booking = getBookedTicket(bookingId);

        if (!PAYMENT_METHOD_CASH.equalsIgnoreCase(booking.getPaymentMethod())) {
            throw new RuntimeException("Ticket is not a cash booking");
        }

        if (booking.getSchedule() == null
                || booking.getSchedule().getBus() == null
                || !matchesCurrentAdmin(booking.getSchedule().getBus().getAdminUserId(), adminUserId)) {
            throw new RuntimeException("Unauthorized ticket access");
        }

        return toTicketResponse(booking);
    }

    private BusTicketResponse toTicketResponse(SeatBooking booking) {
        String qrData = "BOOKING:" + booking.getId() +
                "|CID:" + booking.getApplicantCid() +
                "|SEAT:" + booking.getSeatNumber();

        return BusTicketResponse.builder()
                .bookingId(booking.getId())
                .bookingRef(booking.getBookingRef())
                .paymentRef(booking.getWalletPaymentRef())
                .scheduleId(booking.getSchedule().getId())
                .busNumber(booking.getSchedule().getBus().getBusNumber())
                .busName(booking.getSchedule().getBus().getBusName())
                .source(booking.getSchedule().getRoute().getSource())
                .destination(booking.getSchedule().getRoute().getDestination())
                .departureTime(booking.getSchedule().getDepartureTime())
                .arrivalTime(booking.getSchedule().getArrivalTime())
                .seatNumber(booking.getSeatNumber())
                .seatLabel(booking.getSeatLabel())
                .applicantCid(booking.getApplicantCid())
                .applicantMobile(booking.getApplicantMobile())
                .applicantEmail(booking.getApplicantEmail())
                .status(booking.getStatus().name())
                .qrCodeBase64(generateQrCodeBase64(qrData))
                .build();
    }

    private SeatBooking getAuthorizedBookedTicket(Long bookingId, String userId) {
        SeatBooking booking = getBookedTicket(bookingId);

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized ticket access");
        }

        return booking;
    }

    private SeatBooking getBookedTicket(Long bookingId) {
        SeatBooking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.BOOKED) {
            throw new RuntimeException("Ticket is available only for booked seats");
        }

        return booking;
    }

    private boolean matchesCurrentAdmin(String storedUserId, String currentKeycloakId) {
        if (isBlank(storedUserId) || isBlank(currentKeycloakId)) {
            return false;
        }
        if (storedUserId.equals(currentKeycloakId)) {
            return true;
        }

        boolean storedValueIsUsernameForCurrentUser = appUserService.findByUsername(storedUserId)
                .map(AppUser::getKeycloakId)
                .filter(currentKeycloakId::equals)
                .isPresent();
        if (storedValueIsUsernameForCurrentUser) {
            return true;
        }

        return appUserService.findByKeycloakId(currentKeycloakId)
                .map(AppUser::getUsername)
                .filter(storedUserId::equals)
                .isPresent();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String generateQrCodeBase64(String data) {
        try {
            int size = 300;
            BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "png", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ticket QR code", e);
        }
    }
}
