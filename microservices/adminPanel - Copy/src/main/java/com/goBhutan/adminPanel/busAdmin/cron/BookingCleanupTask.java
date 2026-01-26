package com.goBhutan.adminPanel.busAdmin.cron;

import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BookingCleanupTask {
    private final SeatBookingRepository bookingRepo;

    @Scheduled(fixedRate = 60000) // every 1 min
    public void cleanupExpiredLocks() {
        bookingRepo.releaseExpiredLocks(LocalDateTime.now());
    }
}
