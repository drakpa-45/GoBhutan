package com.goBhutan.adminPanel.taxi.service;


import com.goBhutan.adminPanel.taxi.dto.response.FareBreakdown;
import com.goBhutan.adminPanel.taxi.entity.InterRoute;
import com.goBhutan.adminPanel.taxi.entity.PricingConfig;
import com.goBhutan.adminPanel.taxi.entity.RouteStop;
import com.goBhutan.adminPanel.taxi.enums.TripCategory;
import com.goBhutan.adminPanel.taxi.enums.TripMode;
import com.goBhutan.adminPanel.taxi.repository.PricingConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class FareCalculatorService {

    private final PricingConfigRepository pricingConfigRepo;

    private static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    private static final LocalTime NIGHT_END   = LocalTime.of(5, 0);

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * INTRA + PULL
     * Formula: Base + (distance × ratePerKm) + nightSurcharge
     */
    public FareBreakdown calculateIntraPull(BigDecimal distanceKm, LocalTime pickupTime) {
        PricingConfig cfg = getConfig(TripCategory.INTRA_DZONGKHAG, TripMode.PULL);

        BigDecimal distanceCharge = distanceKm.multiply(cfg.getRatePerKm());
        BigDecimal nightSurcharge = isNightTime(pickupTime) ? cfg.getNightSurcharge() : BigDecimal.ZERO;
        BigDecimal totalFare      = cfg.getBaseFare().add(distanceCharge).add(nightSurcharge);

        return buildPullBreakdown(cfg, cfg.getBaseFare(), distanceCharge, nightSurcharge,
                                  BigDecimal.ZERO, BigDecimal.ONE, totalFare, 1, totalFare);
    }

    /**
     * INTRA + RESERVED
     * Formula: (Base + distance × ratePerKm + nightSurcharge) × (1 + reservedPremium%)
     * Deposit = totalFare × depositPct%
     * Balance = totalFare − deposit
     */
    public FareBreakdown calculateIntraReserved(BigDecimal distanceKm, LocalTime pickupTime) {
        PricingConfig cfg = getConfig(TripCategory.INTRA_DZONGKHAG, TripMode.RESERVED);

        BigDecimal distanceCharge   = distanceKm.multiply(cfg.getRatePerKm());
        BigDecimal nightSurcharge   = isNightTime(pickupTime) ? cfg.getNightSurcharge() : BigDecimal.ZERO;
        BigDecimal subtotal         = cfg.getBaseFare().add(distanceCharge).add(nightSurcharge);
        BigDecimal reservedPremium  = pct(subtotal, cfg.getReservedPremiumPct());
        BigDecimal totalFare        = subtotal.add(reservedPremium);

        return buildReservedBreakdown(cfg, cfg.getBaseFare(), distanceCharge, nightSurcharge,
                                      reservedPremium, BigDecimal.ONE, totalFare, 1, totalFare);
    }

    /**
     * INTER + PULL  (shared seat booking)
     * Fare per seat = routeDistance × ratePerKmPerSeat × surgeMultiplier
     * Total         = farePerSeat × seatsBooked
     * Each passenger is charged farePerSeat individually.
     */
    public FareBreakdown calculateInterPull(InterRoute route, int seatsBooked,
                                            Long boardingStopId, Long alightingStopId) {
        PricingConfig cfg = getConfig(TripCategory.INTER_DZONGKHAG, TripMode.PULL);

        BigDecimal segmentDistance = resolveSegmentDistance(
                route, boardingStopId, alightingStopId);

        BigDecimal surge   = cappedSurge(route.getSurgeMultiplier(), cfg.getMaxSurgeMultiplier());
        BigDecimal perSeat = segmentDistance
                .multiply(route.getRatePerKmPerSeat())
                .multiply(surge)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = perSeat.multiply(BigDecimal.valueOf(seatsBooked));

        return buildPullBreakdown(cfg, BigDecimal.ZERO, total, BigDecimal.ZERO,
                BigDecimal.ZERO, surge, total, seatsBooked, perSeat);
    }
    /**
     * INTER + RESERVED  (full vehicle exclusive booking)
     * Base    = routeDistance × ratePerKmPerSeat × totalSeats  (ALL seats, not just booked)
     * Premium = base × reservedPremiumPct%
     * Total   = base + premium
     * Deposit = total × depositPct%
     */
    public FareBreakdown calculateInterReserved(InterRoute route,
                                                Long boardingStopId, Long alightingStopId) {
        PricingConfig cfg = getConfig(TripCategory.INTER_DZONGKHAG, TripMode.RESERVED);

        BigDecimal segmentDistance = resolveSegmentDistance(
                route, boardingStopId, alightingStopId);

        BigDecimal surge   = cappedSurge(route.getSurgeMultiplier(), cfg.getMaxSurgeMultiplier());
        BigDecimal base    = segmentDistance
                .multiply(route.getRatePerKmPerSeat())
                .multiply(BigDecimal.valueOf(route.getTotalSeats()))
                .multiply(surge)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal premium = pct(base, cfg.getReservedPremiumPct());
        BigDecimal total   = base.add(premium);

        return buildReservedBreakdown(cfg, BigDecimal.ZERO, base, BigDecimal.ZERO,
                premium, surge, total,
                route.getTotalSeats(), route.getRatePerKmPerSeat());
    }


    /**
     * Resolves the segment distance between boarding and alighting stops.
     * Falls back to full route distance if stops are null.
     */
    private BigDecimal resolveSegmentDistance(InterRoute route,
                                              Long boardingStopId, Long alightingStopId) {
        // No stops selected → full route
        if (boardingStopId == null && alightingStopId == null) {
            return route.getRouteDistanceKm();
        }

        List<RouteStop> stops = route.getStops();

        RouteStop boarding = stops.stream()
                .filter(s -> s.getId().equals(boardingStopId))
                .findFirst()
                .orElse(stops.get(0));   // fallback to origin

        RouteStop alighting = stops.stream()
                .filter(s -> s.getId().equals(alightingStopId))
                .findFirst()
                .orElse(stops.get(stops.size() - 1));  // fallback to destination

        // Validate sequence — boarding must come before alighting
        if (boarding.getStopSequence() >= alighting.getStopSequence()) {
            throw new IllegalArgumentException(
                    "Boarding stop must come before alighting stop on the route.");
        }

        BigDecimal segmentDistance = alighting.getDistanceFromOriginKm()
                .subtract(boarding.getDistanceFromOriginKm());

        System.out.println("Segment fare: {} → {} = {} km"+
                boarding.getStopName() + " " + alighting.getStopName()+ " " + segmentDistance);

        return segmentDistance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private FareBreakdown buildPullBreakdown(PricingConfig cfg,
                                              BigDecimal base, BigDecimal distCharge,
                                              BigDecimal nightSurcharge, BigDecimal premium,
                                              BigDecimal surge, BigDecimal total,
                                              int seats, BigDecimal perSeat) {
        BigDecimal commission = pct(total, cfg.getCommissionPct());
        BigDecimal driverNet  = total.subtract(commission);
        return FareBreakdown.builder()
                .baseFare(base)
                .distanceCharge(distCharge)
                .nightSurcharge(nightSurcharge)
                .reservedPremium(BigDecimal.ZERO)
                .surgeMultiplier(surge)
                .totalFare(round(total))
                .depositAmount(null)
                .balanceAmount(null)
                .commissionAmount(round(commission))
                .driverNetAmount(round(driverNet))
                .seatsBooked(seats)
                .farePerSeat(round(perSeat))
                .build();
    }

    private FareBreakdown buildReservedBreakdown(PricingConfig cfg,
                                                  BigDecimal base, BigDecimal distCharge,
                                                  BigDecimal nightSurcharge, BigDecimal premium,
                                                  BigDecimal surge, BigDecimal total,
                                                  int seats, BigDecimal perSeat) {
        BigDecimal deposit    = pct(total, cfg.getDepositPct());
        BigDecimal balance    = total.subtract(deposit);
        BigDecimal commission = pct(total, cfg.getCommissionPct());
        BigDecimal driverNet  = total.subtract(commission);
        return FareBreakdown.builder()
                .baseFare(base)
                .distanceCharge(distCharge)
                .nightSurcharge(nightSurcharge)
                .reservedPremium(premium)
                .surgeMultiplier(surge)
                .totalFare(round(total))
                .depositAmount(round(deposit))
                .balanceAmount(round(balance))
                .commissionAmount(round(commission))
                .driverNetAmount(round(driverNet))
                .seatsBooked(seats)
                .farePerSeat(round(perSeat))
                .build();
    }

    private PricingConfig getConfig(TripCategory category, TripMode mode) {
        return pricingConfigRepo.findByTripCategoryAndTripMode(category, mode)
                .orElseThrow(() -> new IllegalStateException(
                        "Pricing config not found for " + category + " + " + mode));
    }

    private boolean isNightTime(LocalTime time) {
        if (time == null) return false;
        return time.isAfter(NIGHT_START) || time.isBefore(NIGHT_END);
    }

    private BigDecimal cappedSurge(BigDecimal surge, BigDecimal cap) {
        if (surge == null) return BigDecimal.ONE;
        if (cap != null && surge.compareTo(cap) > 0) return cap;
        return surge;
    }

    private BigDecimal pct(BigDecimal base, BigDecimal pct) {
        if (pct == null || pct.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return base.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal round(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val.setScale(2, RoundingMode.HALF_UP);
    }
}
