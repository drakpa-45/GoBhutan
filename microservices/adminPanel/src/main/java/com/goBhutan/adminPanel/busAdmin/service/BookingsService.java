package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.*;
import com.goBhutan.adminPanel.busAdmin.entity.*;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import com.goBhutan.adminPanel.busAdmin.enums.SeatType;
import com.goBhutan.adminPanel.busAdmin.repository.BookingSeatRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BookingsRepo;
import com.goBhutan.adminPanel.busAdmin.repository.BusScheduleRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusSeatConfigRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingsService {

    private final BookingsRepo bookingsRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BusScheduleRepository scheduleRepository;
    private final BusSeatConfigRepository busSeatConfigRepository;

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String adminUserId = principal.getSubject();

        if (!schedule.getActive()) {
            throw new RuntimeException("Schedule is not active");
        }

        if (schedule.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book past schedules");
        }

        List<String> requestedSeats = request.getSeatNumbers();
        List<String> bookedSeats = getBookedSeats(schedule.getId());

        for (String seatNumber : requestedSeats) {
            if (bookedSeats.contains(seatNumber)) {
                throw new RuntimeException("Seat " + seatNumber + " is already booked");
            }
        }

        Bus bus = schedule.getBus();
        List<BusSeatConfig> seatConfigs = busSeatConfigRepository.findByBus_Id(bus.getId());

        for (String seatNumber : requestedSeats) {
            if (!isSeatValid(seatNumber, seatConfigs)) {
                throw new RuntimeException("Invalid seat number: " + seatNumber);
            }
        }

        int requestedSeatCount = requestedSeats.size();
        if (schedule.getAvailableSeats() < requestedSeatCount) {
            throw new RuntimeException("Not enough seats available");
        }

        BigDecimal totalFare = calculateTotalFare(requestedSeats, seatConfigs, schedule.getPrice());

        BusBookings booking = new BusBookings();
        booking.setSchedule(schedule);


        booking.setUserId(adminUserId);
        booking.setPassengerName(request.getPassengerName());
        booking.setEmail(request.getEmail());
        booking.setPhone(request.getPhone());
        booking.setTotalFare(totalFare);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());

        bookingsRepository.save(booking);

        List<BookingSeat> bookingSeats = new ArrayList<>();
        for (String seatNumber : requestedSeats) {
            SeatType seatType = getSeatType(seatNumber, seatConfigs);
            BigDecimal seatFare = calculateSeatFare(seatType, schedule.getPrice());

            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(booking);
            bookingSeat.setSeatNumber(seatNumber);
            bookingSeat.setSeatType(seatType);
            bookingSeat.setFare(seatFare);

            bookingSeats.add(bookingSeat);
        }

        bookingSeatRepository.saveAll(bookingSeats);
        booking.setBookingSeats(bookingSeats);

        schedule.setAvailableSeats(schedule.getAvailableSeats() - requestedSeatCount);
        scheduleRepository.save(schedule);

        return mapToBookingResponse(booking);
    }


    @Transactional
    public AvailableSeatsResponseDTO getAvailableSeats(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        Bus bus = schedule.getBus();
        List<BusSeatConfig> seatConfigs = busSeatConfigRepository.findByBus_Id(bus.getId());
        List<String> bookedSeats = getBookedSeats(scheduleId);

        List<BusSeatAvailabilityDTO> seatAvailability = new ArrayList<>();

        for (BusSeatConfig config : seatConfigs) {
            for (int i = config.getStartNo(); i <= config.getEndNo(); i++) {
                String seatNumber = String.valueOf(i);
                boolean isAvailable = !bookedSeats.contains(seatNumber);
                BigDecimal fare = calculateSeatFare(config.getSeatType(), schedule.getPrice());

                seatAvailability.add(new BusSeatAvailabilityDTO(
                        seatNumber,
                        config.getSeatType(),
                        isAvailable,
                        fare
                ));
            }
        }

        return new AvailableSeatsResponseDTO(
                schedule.getId(),
                bus.getBusNumber(),
                bus.getTotalSeats(),
                schedule.getAvailableSeats(),
                seatAvailability
        );
    }

    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId, String cancellationReason) {
        BusBookings booking = bookingsRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        Schedule schedule = booking.getSchedule();
        if (schedule.getDepartureTime().minusHours(2).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot cancel booking less than 2 hours before departure");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(cancellationReason);

        bookingsRepository.save(booking);

        int seatCount = booking.getBookingSeats().size();
        schedule.setAvailableSeats(schedule.getAvailableSeats() + seatCount);
        scheduleRepository.save(schedule);

        return mapToBookingResponse(booking);
    }
    @Transactional
    public BookingResponseDTO changeSeat(Long bookingId, String oldSeatNumber, String newSeatNumber) {
        BusBookings booking = bookingsRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Can only change seats for confirmed bookings");
        }

        Schedule schedule = booking.getSchedule();

        if (schedule.getDepartureTime().minusHours(1).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot change seats less than 1 hour before departure");
        }

        List<String> bookedSeats = getBookedSeats(schedule.getId());
        if (bookedSeats.contains(newSeatNumber)) {
            throw new RuntimeException("New seat is already booked");
        }

        List<BusSeatConfig> seatConfigs = busSeatConfigRepository.findByBus_Id(schedule.getBus().getId());
        if (!isSeatValid(newSeatNumber, seatConfigs)) {
            throw new RuntimeException("Invalid seat number: " + newSeatNumber);
        }

        BookingSeat bookingSeat = bookingSeatRepository
                .findByBookingIdAndSeatNumber(bookingId, oldSeatNumber)
                .orElseThrow(() -> new RuntimeException("Seat not found in booking"));

        SeatType newSeatType = getSeatType(newSeatNumber, seatConfigs);
        BigDecimal newFare = calculateSeatFare(newSeatType, schedule.getPrice());

        BigDecimal fareDifference = newFare.subtract(bookingSeat.getFare());

        bookingSeat.setSeatNumber(newSeatNumber);
        bookingSeat.setSeatType(newSeatType);
        bookingSeat.setFare(newFare);

        bookingSeatRepository.save(bookingSeat);

        booking.setTotalFare(booking.getTotalFare().add(fareDifference));
        bookingsRepository.save(booking);

        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponseDTO getBookingByReference(String bookingReference) {
        BusBookings booking = bookingsRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponseDTO getBookingById(Long bookingId) {
        BusBookings booking = bookingsRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return mapToBookingResponse(booking);
    }

    @Transactional
    public List<BookingResponseDTO> getUserBookings(String userId) {
        List<BusBookings> bookings = bookingsRepository.findByUserId(userId);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BookingResponseDTO> getBookingsByEmail(String email) {
        List<BusBookings> bookings = bookingsRepository.findByEmail(email);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BookingResponseDTO> getAllBookings() {
        List<BusBookings> bookings = bookingsRepository.findAll();
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    private List<String> getBookedSeats(Long scheduleId) {
        return bookingSeatRepository.findBookedSeatsByScheduleId(scheduleId)
                .stream()
                .map(BookingSeat::getSeatNumber)
                .collect(Collectors.toList());
    }

    private boolean isSeatValid(String seatNumber, List<BusSeatConfig> seatConfigs) {
        try {
            int seatNum = Integer.parseInt(seatNumber);
            return seatConfigs.stream()
                    .anyMatch(config -> seatNum >= config.getStartNo() && seatNum <= config.getEndNo());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private SeatType getSeatType(String seatNumber, List<BusSeatConfig> seatConfigs) {
        int seatNum = Integer.parseInt(seatNumber);
        return seatConfigs.stream()
                .filter(config -> seatNum >= config.getStartNo() && seatNum <= config.getEndNo())
                .findFirst()
                .map(BusSeatConfig::getSeatType)
                .orElse(SeatType.BACK);
    }

    private BigDecimal calculateSeatFare(SeatType seatType, BigDecimal basePrice) {
        switch (seatType) {
            case FRONT:
                return basePrice.multiply(BigDecimal.valueOf(1.3));
            case WINDOW:
                return basePrice.multiply(BigDecimal.valueOf(1.5));
            default:
                return basePrice;
        }
    }

    private BigDecimal calculateTotalFare(List<String> seatNumbers,
                                          List<BusSeatConfig> seatConfigs,
                                          BigDecimal basePrice) {
        BigDecimal total = BigDecimal.ZERO;
        for (String seatNumber : seatNumbers) {
            SeatType seatType = getSeatType(seatNumber, seatConfigs);
            total = total.add(calculateSeatFare(seatType, basePrice));
        }
        return total;
    }

    private BookingResponseDTO mapToBookingResponse(BusBookings booking) {
        Schedule schedule = booking.getSchedule();
        Route route = schedule.getRoute();
        Bus bus = schedule.getBus();

        List<SeatDetailsDTO> seatDetails = booking.getBookingSeats().stream()
                .map(bs -> new SeatDetailsDTO(bs.getSeatNumber(), bs.getSeatType(), bs.getFare()))
                .collect(Collectors.toList());

        return new BookingResponseDTO(
                booking.getId(),
                booking.getBookingReference(),
                booking.getPassengerName(),
                booking.getEmail(),
                booking.getPhone(),
                booking.getTotalFare(),
                booking.getStatus(),
                booking.getBookingDate(),
                booking.getCancelledAt(),
                booking.getCancellationReason(),
                schedule.getId(),
                schedule.getDepartureTime(),
                schedule.getArrivalTime(),
                route.getSource(),
                route.getDestination(),
                bus.getBusNumber(),
                bus.getBusType(),
                seatDetails
        );
    }
}

