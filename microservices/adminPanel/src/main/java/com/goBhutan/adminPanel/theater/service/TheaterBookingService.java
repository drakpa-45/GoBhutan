package com.goBhutan.adminPanel.theater.service;


import com.goBhutan.adminPanel.common.entity.AppUser;
import com.goBhutan.adminPanel.common.repository.AppUserRepository;
import com.goBhutan.adminPanel.theater.dto.*;
import com.goBhutan.adminPanel.theater.entity.*;
import com.goBhutan.adminPanel.theater.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TheaterBookingService {

    private  TheaterBookingRepository bookingRepository;
    private  ScreeningRepository screeningRepository;
    private  SeatRepository seatRepository;
    private AppUserRepository appUserRepository;

    public TheaterBookingService(TheaterBookingRepository bookingRepository,
                          ScreeningRepository screeningRepository,
                          SeatRepository seatRepository,
                          com.goBhutan.adminPanel.common.repository.AppUserRepository appUserRepository) {
        this.bookingRepository = bookingRepository;
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
        this.appUserRepository = appUserRepository;
    }

    public Page<BookingDTO> getAllBookings(Pageable pageable) {
        return bookingRepository.findAllByOrderByBookingDateDesc(pageable)
                .map(this::convertToDTO);
    }

    public Page<BookingDTO> getBookingsByUser(String userId, Pageable pageable) {
        return bookingRepository.findByUserIdOrderByBookingDateDesc(userId, pageable)
                .map(this::convertToDTO);
    }

    public Page<BookingDTO> getBookingsByScreening(String screeningId, Pageable pageable) {
        return bookingRepository.findByScreeningIdOrderByBookingDateDesc(screeningId, pageable)
                .map(this::convertToDTO);
    }

    public Page<BookingDTO> getBookingsByStatus(TheaterBooking.BookingStatus status, Pageable pageable) {
        return bookingRepository.findByStatusOrderByBookingDateDesc(status, pageable)
                .map(this::convertToDTO);
    }

    public BookingDTO getBookingById(String id) {
        TheaterBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return convertToDTO(booking);
    }

    public BookingDTO getBookingByReference(String bookingReference) {
        TheaterBooking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Booking not found with reference: " + bookingReference));
        return convertToDTO(booking);
    }

    public List<BookingDTO> getConfirmedBookingsForScreening(String screeningId) {
        return bookingRepository.findByScreeningIdAndStatus(screeningId, TheaterBooking.BookingStatus.CONFIRMED)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BookingResponseDTO createBooking(BookingCreateDTO createDTO, String userId) {
        // Validate screening
        Screening screening = screeningRepository.findById(createDTO.getScreeningId())
                .orElseThrow(() -> new RuntimeException("Screening not found"));

        if (!screening.getIsActive()) {
            throw new RuntimeException("Screening is not active");
        }

        // Validate user
        AppUser user = appUserRepository.findByKeycloakId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate seats
        List<Seat> seats = seatRepository.findAllById(createDTO.getSeatIds());
        if (seats.size() != createDTO.getSeatIds().size()) {
            throw new RuntimeException("One or more seats not found");
        }

        // Check seat availability and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<TheaterBooking> bookingsToCreate = new ArrayList<>();

        for (Seat seat : seats) {
            // Check if seat is blocked
            if (seat.getIsBlocked()) {
                throw new RuntimeException("Seat " + seat.getRowName() + seat.getSeatNumber() + " is blocked");
            }

            // Check if seat is already booked for this screening
            if (bookingRepository.existsByScreeningIdAndSeatIdAndStatus(
                    createDTO.getScreeningId(), seat.getId(), TheaterBooking.BookingStatus.CONFIRMED)) {
                throw new RuntimeException("Seat " + seat.getRowName() + seat.getSeatNumber() + " is already booked");
            }

            // Calculate price based on seat class
            BigDecimal price;
            switch (seat.getSeatClass()) {
                case VIP:
                    price = screening.getVipPrice();
                    break;
                case STANDARD:
                    price = screening.getStandardPrice();
                    break;
                case ECONOMY:
                    price = screening.getEconomyPrice();
                    break;
                default:
                    throw new RuntimeException("Invalid seat class");
            }

            totalAmount = totalAmount.add(price);

            // Create booking
            TheaterBooking booking = new TheaterBooking();
            booking.setUser(user);
            booking.setScreening(screening);
            booking.setSeat(seat);
            booking.setPricePaid(price);
            booking.setPaymentMethod(createDTO.getPaymentMethod());
            booking.setPaymentReference(createDTO.getPaymentReference());
            booking.setStatus(TheaterBooking.BookingStatus.CONFIRMED);

            bookingsToCreate.add(booking);
        }

        // Update screening availability
        screening.setBookedSeats(screening.getBookedSeats() + seats.size());
        screening.setAvailableSeats(screening.getAvailableSeats() - seats.size());
        screeningRepository.save(screening);

        // Save all bookings
        List<TheaterBooking> savedBookings = bookingRepository.saveAll(bookingsToCreate);

        List<BookingDTO> bookingDTOs = savedBookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new BookingResponseDTO(
                bookingDTOs,
                totalAmount,
                "Booking successful! " + seats.size() + " seat(s) booked."
        );
    }

    public BookingDTO cancelBooking(String id, String reason) {
        TheaterBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        if (booking.getStatus() != TheaterBooking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Only confirmed bookings can be cancelled");
        }

        // Check if screening has already happened
        if (booking.getScreening().getStartTime().isBefore(Instant.now())) {
            throw new RuntimeException("Cannot cancel booking for past screenings");
        }

        booking.setStatus(TheaterBooking.BookingStatus.CANCELLED);
        booking.setIsCancelled(true);
        booking.setCancelledAt(Instant.now());

        // Update screening availability
        Screening screening = booking.getScreening();
        screening.setBookedSeats(screening.getBookedSeats() - 1);
        screening.setAvailableSeats(screening.getAvailableSeats() + 1);
        screeningRepository.save(screening);

        TheaterBooking cancelledBooking = bookingRepository.save(booking);
        return convertToDTO(cancelledBooking);
    }

    public boolean isSeatAvailable(String screeningId, String seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        if (seat.getIsBlocked()) {
            return false;
        }

        return !bookingRepository.existsByScreeningIdAndSeatIdAndStatus(
                screeningId, seatId, TheaterBooking.BookingStatus.CONFIRMED);
    }

    public List<SeatAvailabilityDTO> getAvailableSeatsForScreening(String screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new RuntimeException("Screening not found"));

        List<Seat> allSeats = seatRepository.findByHallIdAndIsActiveTrueOrderByRowNameAscSeatNumberAsc(
                screening.getHall().getId());

        return allSeats.stream()
                .map(seat -> {
                    SeatAvailabilityDTO dto = new SeatAvailabilityDTO();
                    dto.setSeatId(seat.getId());
                    dto.setSeatNumber(seat.getSeatNumber());
                    dto.setRowName(seat.getRowName());
                    dto.setSeatClass(seat.getSeatClass().name());
                    dto.setIsBlocked(seat.getIsBlocked());
                    dto.setIsAvailable(!seat.getIsBlocked() &&
                            !bookingRepository.existsByScreeningIdAndSeatIdAndStatus(
                                    screeningId, seat.getId(), TheaterBooking.BookingStatus.CONFIRMED));

                    // Set price based on seat class
                    switch (seat.getSeatClass()) {
                        case VIP:
                            dto.setPrice(screening.getVipPrice());
                            break;
                        case STANDARD:
                            dto.setPrice(screening.getStandardPrice());
                            break;
                        case ECONOMY:
                            dto.setPrice(screening.getEconomyPrice());
                            break;
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public BookingStatsDTO getBookingStats() {
        Long totalBookings = bookingRepository.count();
        Long confirmedBookings = bookingRepository.countConfirmedBookings();
        Long cancelledBookings = totalBookings - confirmedBookings;
        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        Double occupancyRate = screeningRepository.calculateAverageOccupancyRate();
        if (occupancyRate == null) {
            occupancyRate = 0.0;
        }

        return new BookingStatsDTO(totalBookings, confirmedBookings, cancelledBookings,
                totalRevenue, occupancyRate);
    }

    private BookingDTO convertToDTO(TheaterBooking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingReference(booking.getBookingReference());
        dto.setUserId(booking.getUser().getId());
        dto.setUsername(booking.getUser().getUsername());
        dto.setPricePaid(booking.getPricePaid());
        dto.setStatus(booking.getStatus().name());
        dto.setBookingDate(booking.getBookingDate().toString());
        dto.setPaymentMethod(booking.getPaymentMethod());
        dto.setPaymentReference(booking.getPaymentReference());
        dto.setIsCancelled(booking.getIsCancelled());
        dto.setCancelledAt(booking.getCancelledAt() != null ? booking.getCancelledAt().toString() : null);

        if (booking.getScreening() != null) {
            dto.setScreening(convertScreeningToDTO(booking.getScreening()));
        }

        if (booking.getSeat() != null) {
            dto.setSeat(convertSeatToDTO(booking.getSeat()));
        }

        return dto;
    }

    private ScreeningDTO convertScreeningToDTO(Screening screening) {
        ScreeningDTO dto = new ScreeningDTO();
        dto.setId(screening.getId());
        dto.setScreeningDate(screening.getScreeningDate().toString());
        dto.setStartTime(screening.getStartTime().toString());
        dto.setEndTime(screening.getEndTime().toString());

        if (screening.getMovie() != null) {
            MovieResponseDTO movieDTO = new MovieResponseDTO();
            movieDTO.setId(screening.getMovie().getId());
            movieDTO.setTitle(screening.getMovie().getTitle());
            movieDTO.setPosterUrl(screening.getMovie().getPosterUrl());
            dto.setMovie(movieDTO);
        }

        if (screening.getHall() != null) {
            HallDTO hallDTO = new HallDTO();
            hallDTO.setId(screening.getHall().getId());
            hallDTO.setName(screening.getHall().getName());
            hallDTO.setTheaterName(screening.getHall().getTheater().getName());
            dto.setHall(hallDTO);
        }

        return dto;
    }

    private SeatDTO convertSeatToDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setId(seat.getId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setRowName(seat.getRowName());
        dto.setSeatClass(seat.getSeatClass().name());
        dto.setHallName(seat.getHall().getName());
        return dto;
    }
}
