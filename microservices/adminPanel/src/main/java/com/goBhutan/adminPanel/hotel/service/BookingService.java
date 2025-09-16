package com.goBhutan.adminPanel.hotel.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.goBhutan.adminPanel.hotel.entity.Hotel;
import com.goBhutan.adminPanel.hotel.entity.Room;
import com.goBhutan.adminPanel.hotel.repository.HotelRepository;
import com.goBhutan.adminPanel.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goBhutan.adminPanel.hotel.entity.Booking;
import com.goBhutan.adminPanel.hotel.entity.Booking.BookingStatus;
import com.goBhutan.adminPanel.hotel.repository.BookingRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BookingService {

    @Autowired private BookingRepository bookingRepo;
    @Autowired private HotelRepository hotelRepo;
    @Autowired private RoomRepository roomRepo;

    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    public Booking getBooking(Long id) {
        return bookingRepo.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public Booking createBooking(Long hotelId, Long roomId, Booking booking) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getStatus() != Room.RoomStatus.AVAILABLE) {
            throw new RuntimeException("Room is not available");
        }

        booking.setHotel(hotel);
        booking.setRoom(room);
        booking.setBookingReference(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        return bookingRepo.save(booking);
    }

    public Booking confirmBooking(Long id) {
        Booking booking = getBooking(id);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        return bookingRepo.save(booking);
    }

    public Booking checkIn(Long id) {
        Booking booking = getBooking(id);
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking must be CONFIRMED before check-in");
        }
        booking.setStatus(Booking.BookingStatus.CHECKED_IN);
        Room room = booking.getRoom();
        room.setStatus(Room.RoomStatus.OCCUPIED);
        roomRepo.save(room);
        return bookingRepo.save(booking);
    }

    public Booking checkOut(Long id) {
        Booking booking = getBooking(id);
        if (booking.getStatus() != Booking.BookingStatus.CHECKED_IN) {
            throw new RuntimeException("Booking must be CHECKED_IN before check-out");
        }
        booking.setStatus(Booking.BookingStatus.CHECKED_OUT);
        Room room = booking.getRoom();
        room.setStatus(Room.RoomStatus.AVAILABLE);
        roomRepo.save(room);
        return bookingRepo.save(booking);
    }

    public Booking cancelBooking(Long id) {
        Booking booking = getBooking(id);
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Room room = booking.getRoom();
        room.setStatus(Room.RoomStatus.AVAILABLE);
        roomRepo.save(room);
        return bookingRepo.save(booking);
    }
}