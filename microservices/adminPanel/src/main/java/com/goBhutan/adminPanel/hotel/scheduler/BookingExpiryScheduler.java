package com.goBhutan.adminPanel.hotel.scheduler;

import com.goBhutan.adminPanel.hotel.entity.Booking;
import com.goBhutan.adminPanel.hotel.entity.Room;
import com.goBhutan.adminPanel.hotel.repository.BookingRepository;
import com.goBhutan.adminPanel.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingExpiryScheduler {

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private RoomRepository roomRepo;

    // Runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void expireStaleBookings() {
        List<Booking> expiredBookings = bookingRepo
                .findByStatusAndExpiresAtBefore(
                        Booking.BookingStatus.PENDING,
                        LocalDateTime.now()
                );

        for (Booking booking : expiredBookings) {
            booking.setStatus(Booking.BookingStatus.EXPIRED);
            bookingRepo.save(booking);

            // Free up the room
            Room room = booking.getRoom();
            if (room != null) {
                room.setStatus(Room.RoomStatus.AVAILABLE);
                roomRepo.save(room);
            }
        }

        if (!expiredBookings.isEmpty()) {
            System.out.println("[BookingExpiry] Expired " + expiredBookings.size() + " pending bookings");
        }
    }
}