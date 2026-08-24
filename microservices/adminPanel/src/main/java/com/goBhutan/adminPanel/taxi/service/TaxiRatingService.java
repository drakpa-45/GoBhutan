package com.goBhutan.adminPanel.taxi.service;

import com.goBhutan.adminPanel.taxi.dto.request.TaxiRatingRequest;
import com.goBhutan.adminPanel.taxi.dto.response.TaxiRatingResponse;
import com.goBhutan.adminPanel.taxi.entity.TaxiBooking;
import com.goBhutan.adminPanel.taxi.entity.TaxiRating;
import com.goBhutan.adminPanel.taxi.enums.TaxiBookingStatus;
import com.goBhutan.adminPanel.taxi.repository.TaxiBookingRepository;
import com.goBhutan.adminPanel.taxi.repository.TaxiRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxiRatingService {

    private final TaxiRatingRepository  ratingRepo;
    private final TaxiBookingRepository bookingRepo;

    @Transactional
    public TaxiRatingResponse submitRating(String passengerId, TaxiRatingRequest req) {

        // 1. Booking must exist
        TaxiBooking booking = bookingRepo.findById(req.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Booking not found: " + req.getBookingId()));

        // 2. Only the passenger who made the booking can rate
        if (!booking.getPassengerId().equals(passengerId))
            throw new IllegalStateException("You can only rate your own trips.");

        // 3. Trip must be COMPLETED — cannot rate pending or cancelled trips
        if (booking.getBookingStatus() != TaxiBookingStatus.COMPLETED)
            throw new IllegalStateException(
                    "You can only rate completed trips. " +
                            "Current status: " + booking.getBookingStatus());

        // 4. One rating per booking
        if (ratingRepo.existsByBookingId(req.getBookingId()))
            throw new IllegalStateException("You have already rated this trip.");

        // 5. Driver must have been assigned
        if (booking.getDriverId() == null)
            throw new IllegalStateException("No driver assigned to this booking.");

        TaxiRating rating = TaxiRating.builder()
                .bookingId(req.getBookingId())
                .passengerId(passengerId)
                .driverId(booking.getDriverId())
                .rating(req.getRating())
                .comment(req.getComment())
                .build();

        return toResponse(ratingRepo.save(rating));
    }

    /** Get rating for a specific booking */
    public TaxiRatingResponse getRatingByBooking(Long bookingId) {
        return ratingRepo.findByBookingId(bookingId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No rating found for booking: " + bookingId));
    }

    /** All ratings for a driver + average score */
    public Map<String, Object> getDriverRatings(String driverId) {
        List<TaxiRatingResponse> ratings = ratingRepo
                .findByDriverIdOrderByCreatedAtDesc(driverId)
                .stream().map(this::toResponse).collect(Collectors.toList());

        Double avg = ratingRepo.findAverageRatingByDriverId(driverId);
        long   count = ratingRepo.countByDriverId(driverId);

        return Map.of(
                "driverId",    driverId,
                "averageRating", avg != null
                        ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO,
                "totalRatings", count,
                "ratings",      ratings
        );
    }

    private TaxiRatingResponse toResponse(TaxiRating r) {
        return TaxiRatingResponse.builder()
                .id(r.getId())
                .bookingId(r.getBookingId())
                .driverId(r.getDriverId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}