package com.goBhutan.adminPanel.busAdmin.cron;

import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import com.goBhutan.adminPanel.busAdmin.service.SeatBroadcastService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingCleanupTask {
    private static final Logger log = LoggerFactory.getLogger(BookingCleanupTask.class);
    private final SeatBookingRepository bookingRepo;
    private final SeatBroadcastService broadcastService;
    @Value("${app.clients.bus.release-expired-lock:false}")
    private boolean releaseExpiredLock;

    @PostConstruct
    public void logSchedulerInitialization() {
        log.info("booking-cleanup-task initialized releaseExpiredLock={}", releaseExpiredLock);
    }

    @Scheduled(fixedRate = 60000) // every 1 min
    @Transactional
    public void cleanupExpiredLocks() {
        if (!releaseExpiredLock) {
            return;
        }

        LocalDateTime startedAt = LocalDateTime.now();
        long startMillis = System.currentTimeMillis();
        log.info("expired-lock-cleanup start at={}", startedAt);

        List<SeatBooking> expiredLocks = bookingRepo.findExpiredLockedSeats(startedAt);

        if (expiredLocks.isEmpty()) {
            log.info(
                    "expired-lock-cleanup end at={} released=0 durationMs={}",
                    LocalDateTime.now(),
                    System.currentTimeMillis() - startMillis);
            return;
        }

        for (SeatBooking booking : expiredLocks) {
            log.info(
                    "expired-lock-release bookingId={} scheduleId={} seat={} label={} userId={} bookingRef={} walletPaymentRef={} expiredAt={}",
                    booking.getId(),
                    booking.getSchedule().getId(),
                    booking.getSeatNumber(),
                    booking.getSeatLabel(),
                    booking.getUserId(),
                    booking.getBookingRef(),
                    booking.getWalletPaymentRef(),
                    booking.getLockExpiry());
        }

        Set<Long> scheduleIds = expiredLocks.stream()
                .map(booking -> booking.getSchedule().getId())
                .collect(Collectors.toSet());
        int released = bookingRepo.releaseExpiredLocks(startedAt);
        scheduleIds.forEach(broadcastService::broadcastSeatUpdate);
        log.info(
                "expired-lock-cleanup end at={} detected={} released={} durationMs={}",
                LocalDateTime.now(),
                expiredLocks.size(),
                released,
                System.currentTimeMillis() - startMillis);
    }
}
