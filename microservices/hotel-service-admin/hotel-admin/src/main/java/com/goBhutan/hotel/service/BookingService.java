package com.goBhutan.hotel.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goBhutan.hotel.entity.Booking;
import com.goBhutan.hotel.entity.Booking.BookingStatus;
import com.goBhutan.hotel.repository.BookingRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    public List<Booking> getBookingsByAdmin(String adminUserId) {
        return bookingRepository.findBookingsByAdminUserId(adminUserId);
    }
    
    public List<Booking> getBookingsByHotel(Long hotelId) {
        return bookingRepository.findByHotelId(hotelId);
    }
    
    public List<Booking> getTodayCheckIns(Long hotelId) {
        return bookingRepository.findTodayCheckIns(hotelId, LocalDate.now());
    }
    
    public List<Booking> getTodayCheckOuts(Long hotelId) {
        return bookingRepository.findTodayCheckOuts(hotelId, LocalDate.now());
    }
    
    public Map<BookingStatus, Long> getBookingStatusCounts(Long hotelId) {
        return java.util.Arrays.stream(BookingStatus.values())
                .collect(Collectors.toMap(
                    status -> status,
                    status -> bookingRepository.countBookingsByHotelAndStatus(hotelId, status)
                ));
    }
    
    public List<Booking> getBookingsByStatus(Long hotelId, BookingStatus status) {
        return bookingRepository.findByHotelIdAndStatus(hotelId, status);
    }
}