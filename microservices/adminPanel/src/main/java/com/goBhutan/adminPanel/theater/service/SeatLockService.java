package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.seat.SeatLockRequestDTO;
import com.goBhutan.adminPanel.theater.dto.seat.SeatLockResponseDTO;
import com.goBhutan.adminPanel.theater.dto.seat.ShowtimeLockedSeatsDTO;
import com.goBhutan.adminPanel.theater.entity.Seat;
import com.goBhutan.adminPanel.theater.entity.SeatLock;
import com.goBhutan.adminPanel.theater.repository.SeatLockRepository;
import com.goBhutan.adminPanel.theater.repository.SeatRepository; // your existing repo
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockService {

    private static final int LOCK_DURATION_MINUTES = 3;

    private final SeatLockRepository seatLockRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public SeatLockResponseDTO toggleSeatLock(SeatLockRequestDTO request) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Load seat eagerly with hall and seatClass
        Seat seat = seatRepository.findByIdWithHallAndClass(request.getSeatId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Seat not found: " + request.getSeatId()));

        // 2. Validate hall matches
        if (!seat.getHall().getId().equals(request.getHallId())) {
            throw new IllegalArgumentException(
                    "Seat " + seat.getSeatIdentifier() +
                            " does not belong to hall " + request.getHallId());
        }

        // 3. Validate seatClass matches
        if (!seat.getSeatClass().getId().equals(request.getSeatClassId())) {
            throw new IllegalArgumentException(
                    "Seat " + seat.getSeatIdentifier() +
                            " is not in seat class " + request.getSeatClassId());
        }

        // 4. Reject permanently blocked seats
        if (Boolean.TRUE.equals(seat.getIsBlocked())) {
            throw new IllegalStateException(
                    "Seat " + seat.getSeatIdentifier() +
                            " is blocked: " + seat.getBlockReason());
        }

        Optional<SeatLock> existingLock = seatLockRepository
                .findActiveLock(seat.getId(), request.getScreenId(), now);

        // ── Active lock exists ─────────────────────────────────────────
        if (existingLock.isPresent()) {
            SeatLock lock = existingLock.get();

            if (lock.getLockedByUserId().equals(request.getUserId())) {
                // Same user re-clicked → UNLOCK
                seatLockRepository.delete(lock);
                log.info("Seat [{}] class [{}] hall [{}] UNLOCKED by user [{}]",
                        seat.getSeatIdentifier(),
                        seat.getSeatClass().getName(),
                        seat.getHall().getName(),
                        request.getUserId());

                return buildResponse(seat, request.getScreenId(), "UNLOCKED", null, 0);
            }

            // Different user → reject
            long secondsLeft = ChronoUnit.SECONDS.between(now, lock.getExpiresAt());
            throw new IllegalStateException(
                    "Seat " + seat.getSeatIdentifier() +
                            " [" + seat.getSeatClass().getName() + "]" +
                            " in hall " + seat.getHall().getName() +
                            " is locked by another user. Available in " + secondsLeft + "s.");
        }

        // ── No lock → LOCK it ──────────────────────────────────────────
        LocalDateTime expiresAt = now.plusMinutes(LOCK_DURATION_MINUTES);

        SeatLock newLock = SeatLock.builder()
                .seat(seat)
                .screenId(request.getScreenId())
                .lockedByUserId(request.getUserId())
                .lockedAt(now)
                .expiresAt(expiresAt)
                .build();

        seatLockRepository.save(newLock);
        log.info("Seat [{}] class [{}] hall [{}] LOCKED by user [{}] until [{}]",
                seat.getSeatIdentifier(),
                seat.getSeatClass().getName(),
                seat.getHall().getName(),
                request.getUserId(),
                expiresAt);

        return buildResponse(seat, request.getScreenId(), "LOCKED", expiresAt,
                LOCK_DURATION_MINUTES * 60L);
    }

    // ── Seat map: locked seats grouped by class for a hall+showtime ────
    @Transactional(readOnly = true)
    public ShowtimeLockedSeatsDTO getLockedSeats(Long screenId, Long hallId) {
        List<SeatLock> locks = seatLockRepository
                .findAllActiveByShowtimeAndHall(screenId, hallId, LocalDateTime.now());

        // Group locked seat IDs by class name
        Map<String, List<Long>> byClass = locks.stream()
                .collect(Collectors.groupingBy(
                        sl -> sl.getSeat().getSeatClass().getName(),
                        Collectors.mapping(sl -> sl.getSeat().getId(), Collectors.toList())
                ));

        return ShowtimeLockedSeatsDTO.builder()
                .screenId(screenId)
                .hallId(hallId)
                .lockedSeatIdsByClass(byClass)
                .build();
    }

    // ── Booking guard ──────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public void assertUserHoldsLocks(Long showtimeId, String userId, List<Long> seatIds) {
        List<Long> heldSeatIds = seatLockRepository
                .findActiveLocksForUserSeats(showtimeId, userId, seatIds, LocalDateTime.now())
                .stream()
                .map(sl -> sl.getSeat().getId())
                .toList();

        List<Long> missing = seatIds.stream()
                .filter(id -> !heldSeatIds.contains(id))
                .toList();

        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Lock expired or missing for seat IDs: " + missing +
                            ". Please re-select.");
        }
    }

    // ── Release locks after booking ────────────────────────────────────
    @Transactional
    public void releaseLocksAfterBooking(Long showtimeId, String userId, List<Long> seatIds) {
        seatLockRepository
                .findActiveLocksForUserSeats(showtimeId, userId, seatIds, LocalDateTime.now())
                .forEach(seatLockRepository::delete);
    }

    // ── Scheduler ──────────────────────────────────────────────────────
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void purgeExpiredLocks() {
        int deleted = seatLockRepository.deleteExpiredLocks(LocalDateTime.now());
        if (deleted > 0) log.info("Purged {} expired seat locks", deleted);
    }

    // ── Private builder ────────────────────────────────────────────────
    private SeatLockResponseDTO buildResponse(Seat seat, Long screenId,
                                              String status, LocalDateTime expiresAt,
                                              long secondsRemaining) {
        return SeatLockResponseDTO.builder()
                .seatId(seat.getId())
                .seatIdentifier(seat.getSeatIdentifier())
                .screenId(screenId)
                .hallId(seat.getHall().getId())
                .hallName(seat.getHall().getName())
                .seatClassId(seat.getSeatClass().getId())
                .seatClassName(seat.getSeatClass().getName())
                .seatPrice(seat.getBasePrice())
                .status(status)
                .expiresAt(expiresAt)
                .secondsRemaining(secondsRemaining)
                .build();
    }
}