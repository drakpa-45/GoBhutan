package com.goBhutan.adminPanel.busAdmin.cron;

import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.service.BusScheduleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingScheduleTask {

    private static final Logger log = LoggerFactory.getLogger(BookingScheduleTask.class);

    private final BusScheduleService busScheduleService;

    @Value("${app.clients.bus.booking-schedule.enabled:true}")
    private boolean bookingScheduleEnabled;

    @PostConstruct
    public void logSchedulerInitialization() {
        log.info("booking-schedule-task initialized enabled={}", bookingScheduleEnabled);
    }

    @Scheduled(cron = "${app.clients.bus.booking-schedule.cron:0 0 23 * * *}", zone = "${app.clients.bus.booking-schedule.zone:Asia/Thimphu}")
    public void generateNextBusSchedules() {
        if (!bookingScheduleEnabled) {
            return;
        }

        LocalDateTime startedAt = LocalDateTime.now();
        long startMillis = System.currentTimeMillis();
        log.info("booking-schedule start at={}", startedAt);

        try {
            List<Schedule> schedules = busScheduleService.generateNextSchedulesForAllActiveBuses();
            log.info(
                    "booking-schedule end at={} generatedOrUpdated={} durationMs={}",
                    LocalDateTime.now(),
                    schedules.size(),
                    System.currentTimeMillis() - startMillis);
        } catch (Exception ex) {
            log.error(
                    "booking-schedule failed at={} durationMs={} reason={}",
                    LocalDateTime.now(),
                    System.currentTimeMillis() - startMillis,
                    ex.getMessage(),
                    ex);
        }
    }
}
