package com.goBhutan.adminPanel.taxi.scheduler;

import com.goBhutan.adminPanel.taxi.repository.DriverLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Runs every 60 seconds.
 * Marks any driver offline if their last ping was more than 2 minutes ago.
 *
 * Why 2 minutes for a 30-second interval?
 * Allows one missed ping (network hiccup) before flagging offline.
 * If driver kills the app, they'll be marked offline within 2 minutes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StaleDriverCleanupScheduler {

    private final DriverLocationRepository driverLocationRepo;

    @Scheduled(fixedDelay = 60_000)   // every 60 seconds
    @Transactional
    @Modifying
    public void markStaleDriversOffline() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(120);

        // Load and update in-memory (small dataset — typically < 200 active drivers)
        var stale = driverLocationRepo.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getIsOnline())
                          && d.getLastUpdatedAt() != null
                          && d.getLastUpdatedAt().isBefore(cutoff))
                .toList();

        if (!stale.isEmpty()) {
            stale.forEach(d -> {
                d.setIsOnline(false);
                log.info("Driver {} marked offline — last ping: {}", d.getDriverId(), d.getLastUpdatedAt());
            });
            driverLocationRepo.saveAll(stale);
            log.info("Marked {} driver(s) offline due to stale ping.", stale.size());
        }
    }
}
