package com.goBhutan.adminPanel.theater.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "tbl_mvth_seat_locks",
        indexes = {
                @Index(name = "idx_seat_lock_seat_showtime", columnList = "seat_id, screen_id"),
                @Index(name = "idx_seat_lock_expires", columnList = "expires_at")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Direct FK to your Seat entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "screen_id", nullable = false)
    private Long screenId;

    @Column(name = "locked_by_user_id", nullable = false)
    private String lockedByUserId;

    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}