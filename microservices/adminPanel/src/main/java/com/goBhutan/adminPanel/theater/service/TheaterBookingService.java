package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.paymentInt.dto.ServicePaymentRequest;
import com.goBhutan.adminPanel.theater.dto.booking.BookingRequestDTO;
import com.goBhutan.adminPanel.theater.dto.booking.TicketResponseDTO;
import com.goBhutan.adminPanel.theater.entity.*;
import com.goBhutan.adminPanel.theater.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TheaterBookingService {

    private final TheaterBookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final SeatStatusRepository seatStatusRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final SeatLockService seatLockService;          // ← injected

    @Transactional
    public List<TicketResponseDTO> bookTickets(BookingRequestDTO request) {

        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new IllegalArgumentException("Screening not found"));

        Theater theater = screening.getHall().getTheater();

        SeatStatus availableStatus = seatStatusRepository
                .findByStatusNameIgnoreCase("AVAILABLE")
                .orElseThrow(() -> new IllegalStateException("Seat status AVAILABLE not configured"));

        SeatStatus bookedStatus = seatStatusRepository
                .findByStatusNameIgnoreCase("BOOKED")
                .orElseThrow(() -> new IllegalStateException("Seat status BOOKED not configured"));

        TheaterBookingStatus createdStatus = bookingStatusRepository
                .findByStatusNameIgnoreCase("CREATED")
                .orElseThrow(() -> new IllegalStateException("BookingStatus CREATED not configured"));

        // ✅ STEP 1 — Validate user holds a valid (non-expired) seat lock
        //            for every seat in this request, scoped to screening+hall+class
        List<Long> requestedSeatIds = request.getTickets().stream()
                .map(BookingRequestDTO.TicketRequest::getSeatId)
                .collect(Collectors.toList());

        seatLockService.assertUserHoldsLocks(
                request.getScreeningId(),   // used as showtimeId in SeatLock
                request.getUserId(),        // add String userId to BookingRequestDTO
                requestedSeatIds
        );

        // Build booking
        TheaterBooking booking = new TheaterBooking();
        booking.setBookingRef(generateBookingRef());
        booking.setScreening(screening);
        booking.setTheater(theater);
        booking.setBookingStatus(createdStatus);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Ticket> ticketsToSave = new ArrayList<>();
        List<TicketResponseDTO> response = new ArrayList<>();

        for (BookingRequestDTO.TicketRequest t : request.getTickets()) {

            // 🔒 Row-level DB lock (unchanged — still needed as final safety net)
            Seat seat = seatRepository.findByIdForUpdate(t.getSeatId())
                    .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

            // ✅ STEP 2 — Double-check seat is still AVAILABLE at DB level
            //            (covers edge case: seat was booked outside lock flow)
            if (!seat.getStatus().getId().equals(availableStatus.getId())) {
                // Release all locks for this user since booking cannot proceed
                seatLockService.releaseLocksAfterBooking(
                        request.getScreeningId(),
                        request.getUserId(),
                        requestedSeatIds
                );
                throw new IllegalArgumentException(
                        "Seat " + seat.getSeatIdentifier() + " is no longer available"
                );
            }

            seat.setStatus(bookedStatus);
            seatRepository.save(seat);

            BigDecimal seatPrice = seat.getBasePrice();
            totalAmount = totalAmount.add(seatPrice);

            String ticketNumber =
                    booking.getBookingRef() + "-"
                            + seat.getSeatClass().getName()
                            + seat.getRowName()
                            + seat.getSeatNumber();

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeat(seat);
            ticket.setTicketNumber(ticketNumber);
            ticket.setCustomerName(t.getCustomerName());
            ticket.setCidOrPassport(t.getCidOrPassport());
            ticket.setPhoneNumber(t.getPhoneNumber());
            ticket.setEmail(t.getEmail());
            ticket.setScreening(screening);

            ticketsToSave.add(ticket);

            response.add(new TicketResponseDTO(
                    ticketNumber,
                    seat.getId(),
                    seat.getSeatIdentifier(),
                    seat.getSeatClass().getName(),
                    t.getCustomerName(),
                    t.getCidOrPassport(),
                    t.getPhoneNumber(),
                    t.getEmail(),
                    ticket.getCreatedAt(),
                    screening.getId(),
                    screening.getMovieName()
            ));
        }

        booking.setTotalAmount(totalAmount);
        booking.setTickets(ticketsToSave);
        bookingRepository.save(booking);

        // ✅ STEP 3 — Release seat locks now that seats are BOOKED
        //            so lock table stays clean (scheduler also covers this)
        seatLockService.releaseLocksAfterBooking(
                request.getScreeningId(),
                request.getUserId(),
                requestedSeatIds
        );

        return response;
    }

    // FETCH BOOKINGS BY THEATER ID (unchanged)
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getBookingsByTheaterId(Long theaterId) {
        List<Ticket> tickets = ticketRepository.findByBooking_Theater_Id(theaterId);

        return tickets.stream()
                .map(ticket -> {
                    Seat seat = ticket.getSeat();
                    Screening screening = ticket.getScreening();
                    return new TicketResponseDTO(
                            ticket.getTicketNumber(),
                            seat.getId(),
                            seat.getSeatIdentifier(),
                            seat.getSeatClass().getName(),
                            ticket.getCustomerName(),
                            ticket.getCidOrPassport(),
                            ticket.getPhoneNumber(),
                            ticket.getEmail(),
                            ticket.getCreatedAt(),
                            screening.getId(),
                            screening.getMovieName()
                    );
                })
                .collect(Collectors.toList());
    }

    public static String generateBookingRef() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "BK-" + date + "-" + random;
    }

    // CANCEL SINGLE TICKET
    public void cancelTicket(String ticketNumber) {

        Ticket ticket = ticketRepository.findByTicketNumberForUpdate(ticketNumber)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        Seat seat = ticket.getSeat();

        SeatStatus available = seatStatusRepository.findByStatusNameIgnoreCase("AVAILABLE")
                .orElseThrow(() -> new IllegalStateException("SeatStatus AVAILABLE missing"));

        TheaterBookingStatus cancelStatus = bookingStatusRepository
                .findByStatusNameIgnoreCase("CANCEL")
                .orElseThrow(() -> new IllegalStateException("BookingStatus CANCEL not configured"));

        seat.setStatus(available);
        seatRepository.save(seat);

        ticketRepository.delete(ticket);

        TheaterBooking booking = ticket.getBooking();
        if (booking.getTickets().isEmpty()) {
            booking.setBookingStatus(cancelStatus);
            bookingRepository.save(booking);
        }
    }

    // CANCEL ENTIRE BOOKING
    public void cancelBooking(String bookingRef) {

        TheaterBooking booking = bookingRepository.findByBookingRefForUpdate(bookingRef)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        SeatStatus available = seatStatusRepository.findByStatusNameIgnoreCase("AVAILABLE")
                .orElseThrow(() -> new IllegalStateException("SeatStatus AVAILABLE missing"));

        TheaterBookingStatus cancelStatus = bookingStatusRepository
                .findByStatusNameIgnoreCase("CANCEL")
                .orElseThrow(() -> new IllegalStateException("BookingStatus CANCEL not configured"));

        for (Ticket ticket : booking.getTickets()) {
            Seat seat = ticket.getSeat();
            seat.setStatus(available);
            seatRepository.save(seat);
        }

        ticketRepository.deleteAll(booking.getTickets());

        booking.setBookingStatus(cancelStatus);
        bookingRepository.save(booking);
    }

    public ServicePaymentRequest buildDirectGatewayPaymentRequest(
            String bookingRef, String userId,
            BigDecimal amount, String currency, String description) {

        TheaterBooking booking = bookingRepository.findByBookingRef(bookingRef)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingRef));

        ServicePaymentRequest request = new ServicePaymentRequest();
        request.setAmount(amount != null ? amount : booking.getTotalAmount());
        request.setCurrency(currency != null ? currency : "BTN");
        request.setServiceName("THEATER");
        request.setReferenceType("THEATER_BOOKING");
        request.setReferenceId(bookingRef);
        request.setDescription(description != null ? description : "Theater ticket booking payment");
        return request;
    }

    public void extendDirectGatewayPaymentLock(String bookingRef, String userId, LocalDateTime expiresAt) {
        TheaterBooking booking = bookingRepository.findByBookingRef(bookingRef)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingRef));
        booking.setExpiresAt(expiresAt);
        bookingRepository.save(booking);
    }

    public String ensureDirectGatewayPaymentCanContinue(String paymentRef, String userId) {
        TheaterBooking booking = bookingRepository.findByWalletPaymentRef(paymentRef)
                .orElseThrow(() -> new RuntimeException("Booking not found for paymentRef: " + paymentRef));

        if (booking.isExpired()) {
            throw new RuntimeException("Booking has expired: " + booking.getBookingRef());
        }

        TheaterBookingStatus pendingStatus = bookingStatusRepository
                .findByStatusNameIgnoreCase("CREATED")
                .orElseThrow(() -> new IllegalStateException("BookingStatus CREATED not configured"));

        if (!booking.getBookingStatus().getId().equals(pendingStatus.getId())) {
            throw new RuntimeException("Booking is not in CREATED state: " + booking.getBookingStatus().getStatusName());
        }

        return booking.getBookingRef();
    }

    public String ensureDirectGatewayPaymentCanDebit(String paymentRef, String userId) {
        return ensureDirectGatewayPaymentCanContinue(paymentRef, userId);
    }

    @Transactional
    public List<Ticket> confirmDirectGatewayPaymentBooking(String paymentRef, String userId) {
        TheaterBooking booking = bookingRepository.findByWalletPaymentRef(paymentRef)
                .orElseThrow(() -> new RuntimeException("Booking not found for paymentRef: " + paymentRef));

        TheaterBookingStatus confirmedStatus = bookingStatusRepository
                .findByStatusNameIgnoreCase("CONFIRMED")
                .orElseThrow(() -> new IllegalStateException("BookingStatus CONFIRMED not configured"));

        booking.setBookingStatus(confirmedStatus);
        booking.setPaymentMethod("DIRECT_GATEWAY");
        bookingRepository.save(booking);

        return booking.getTickets();
    }

}