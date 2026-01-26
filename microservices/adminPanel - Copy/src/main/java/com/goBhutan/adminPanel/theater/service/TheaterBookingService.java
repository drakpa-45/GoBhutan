package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.booking.BookingRequestDTO;
import com.goBhutan.adminPanel.theater.dto.booking.TicketResponseDTO;
import com.goBhutan.adminPanel.theater.entity.*;
import com.goBhutan.adminPanel.theater.entity.TheaterBooking;
import com.goBhutan.adminPanel.theater.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TheaterBookingService {

    private final TheaterBookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final SeatStatusRepository seatStatusRepository;

    @Transactional
    public List<TicketResponseDTO> bookTickets(BookingRequestDTO request) {

        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new IllegalArgumentException("Screening not found"));

        SeatStatus availableStatus = seatStatusRepository
                .findByStatusNameIgnoreCase("AVAILABLE")
                .orElseThrow(() -> new IllegalStateException("Seat status AVAILABLE not configured"));

        SeatStatus bookedStatus = seatStatusRepository
                .findByStatusNameIgnoreCase("BOOKED")
                .orElseThrow(() -> new IllegalStateException("Seat status BOOKED not configured"));

        TheaterBooking booking = new TheaterBooking();
        booking.setScreening(screening);

        List<Ticket> ticketsToSave = new ArrayList<>();
        List<TicketResponseDTO> response = new ArrayList<>();

        for (BookingRequestDTO.TicketRequest t : request.getTickets()) {

            // 🔒 Lock seat row
            Seat seat = seatRepository.findByIdForUpdate(t.getSeatId())
                    .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

            // ✅ Check availability
            if (!seat.getStatus().getId().equals(availableStatus.getId())) {
                throw new IllegalArgumentException(
                        "Seat " + seat.getSeatIdentifier() + " is already booked"
                );
            }

            // ✅ Mark seat as BOOKED
            seat.setStatus(bookedStatus);
            seatRepository.save(seat);

            // 🎟 Ticket number generation
            String ticketNumber =
                    seat.getSeatClass().getName()
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

        booking.setTickets(ticketsToSave);
        bookingRepository.save(booking);
        ticketRepository.saveAll(ticketsToSave);

        return response;
    }

    // CANCEL SINGLE TICKET
    public void cancelTicket(String ticketNumber) {

        Ticket ticket = ticketRepository.findByTicketNumberForUpdate(ticketNumber)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        Seat seat = ticket.getSeat();

        // Free seat
        SeatStatus available = seatStatusRepository.findByStatusNameIgnoreCase("AVAILABLE")
                .orElseThrow(() -> new IllegalStateException("SeatStatus AVAILABLE missing"));

        seat.setStatus(available);
        seatRepository.save(seat);

        // Remove ticket
        ticketRepository.delete(ticket);

        // Update booking if no tickets left
        TheaterBooking booking = ticket.getBooking();
        if (booking.getTickets().isEmpty()) {
            booking.setBookingStatus(TheaterBooking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }
    }

    // CANCEL ENTIRE BOOKING
    public void cancelBooking(String bookingRef) {

        TheaterBooking booking = bookingRepository.findByBookingRefForUpdate(bookingRef)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        SeatStatus available = seatStatusRepository.findByStatusNameIgnoreCase("AVAILABLE")
                .orElseThrow(() -> new IllegalStateException("SeatStatus AVAILABLE missing"));

        for (Ticket ticket : booking.getTickets()) {
            Seat seat = ticket.getSeat();
            seat.setStatus(available);
            seatRepository.save(seat);
        }

        ticketRepository.deleteAll(booking.getTickets());

        booking.setBookingStatus(TheaterBooking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}

