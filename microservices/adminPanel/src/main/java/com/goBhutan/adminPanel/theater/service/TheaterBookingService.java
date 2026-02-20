package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.booking.BookingRequestDTO;
import com.goBhutan.adminPanel.theater.dto.booking.TicketResponseDTO;
import com.goBhutan.adminPanel.theater.entity.*;
import com.goBhutan.adminPanel.theater.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Transactional
    public List<TicketResponseDTO> bookTickets(BookingRequestDTO request) {

        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new IllegalArgumentException("Screening not found"));

        // Get theater from screening
        Theater theater = screening.getHall().getTheater();

        SeatStatus availableStatus = seatStatusRepository
                .findByStatusNameIgnoreCase("AVAILABLE")
                .orElseThrow(() -> new IllegalStateException("Seat status AVAILABLE not configured"));

        SeatStatus bookedStatus = seatStatusRepository
                .findByStatusNameIgnoreCase("BOOKED")
                .orElseThrow(() -> new IllegalStateException("Seat status BOOKED not configured"));

        TheaterBookingStatus createdStatus = bookingStatusRepository
                .findByStatusNameIgnoreCase("CREATED")
                .orElseThrow(() ->
                        new IllegalStateException("BookingStatus CREATED not configured")
                );

        // ✅ Generate bookingRef and set theater
        TheaterBooking booking = new TheaterBooking();
        booking.setBookingRef(generateBookingRef());
        booking.setScreening(screening);
        booking.setTheater(theater);
        booking.setBookingStatus(createdStatus);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<Ticket> ticketsToSave = new ArrayList<>();
        List<TicketResponseDTO> response = new ArrayList<>();

        for (BookingRequestDTO.TicketRequest t : request.getTickets()) {

            // 🔒 Row-level lock
            Seat seat = seatRepository.findByIdForUpdate(t.getSeatId())
                    .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

            if (!seat.getStatus().getId().equals(availableStatus.getId())) {
                throw new IllegalArgumentException(
                        "Seat " + seat.getSeatIdentifier() + " is already booked"
                );
            }

            seat.setStatus(bookedStatus);
            seatRepository.save(seat);

            BigDecimal seatPrice = seat.getBasePrice();
            totalAmount = totalAmount.add(seatPrice);

            // 🎟 Ticket Number
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
                    ticket.getCreatedAt()
            ));
        }

        booking.setTotalAmount(totalAmount);
        booking.setTickets(ticketsToSave);
        bookingRepository.save(booking); // cascades tickets

        return response;
    }

    // FETCH BOOKINGS BY THEATER ID
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getBookingsByTheaterId(Long theaterId) {
        List<Ticket> tickets = ticketRepository.findByBooking_Theater_Id(theaterId);

        return tickets.stream()
                .map(ticket -> {
                    Seat seat = ticket.getSeat();
                    return new TicketResponseDTO(
                            ticket.getTicketNumber(),
                            seat.getId(),
                            seat.getSeatIdentifier(),
                            seat.getSeatClass().getName(),
                            ticket.getCustomerName(),
                            ticket.getCidOrPassport(),
                            ticket.getPhoneNumber(),
                            ticket.getEmail(),
                            ticket.getCreatedAt()
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

        // Free seat
        SeatStatus available = seatStatusRepository.findByStatusNameIgnoreCase("AVAILABLE")
                .orElseThrow(() -> new IllegalStateException("SeatStatus AVAILABLE missing"));

        TheaterBookingStatus cancelStatus = bookingStatusRepository
                .findByStatusNameIgnoreCase("CANCEL")
                .orElseThrow(() ->
                        new IllegalStateException("BookingStatus CANCEL not configured")
                );

        seat.setStatus(available);
        seatRepository.save(seat);

        // Remove ticket
        ticketRepository.delete(ticket);

        // Update booking if no tickets left
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
                .orElseThrow(() ->
                        new IllegalStateException("BookingStatus CANCEL not configured")
                );

        for (Ticket ticket : booking.getTickets()) {
            Seat seat = ticket.getSeat();
            seat.setStatus(available);
            seatRepository.save(seat);
        }

        ticketRepository.deleteAll(booking.getTickets());

        booking.setBookingStatus(cancelStatus);
        bookingRepository.save(booking);
    }
}