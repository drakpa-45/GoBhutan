package com.goBhutan.adminPanel.taxi.service;

import com.goBhutan.adminPanel.taxi.entity.DropPoint;
import com.goBhutan.adminPanel.taxi.entity.Dzongkhag;
import com.goBhutan.adminPanel.taxi.repository.DropPointRepository;
import com.goBhutan.adminPanel.taxi.repository.DzongkhagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DropPointService {

    private final DropPointRepository dropPointRepo;
    private final DzongkhagRepository dzongkhagRepo;

    /**
     * Returns only drop points that fall within the corridor
     * between origin and destination.
     *
     * Algorithm:
     *  1. Get lat/lng of origin and destination dzongkhag
     *  2. For each active drop point, compute perpendicular
     *     distance from the origin→destination line
     *  3. Return only those within bufferKm of the line
     */
    public List<DropPoint> getDropPointsInCorridor(
            Long originDzongkhagId,
            Long destinationDzongkhagId,
            double bufferKm) {

        Dzongkhag origin = dzongkhagRepo.findById(originDzongkhagId)
                .orElseThrow(() -> new IllegalArgumentException("Origin not found"));
        Dzongkhag destination = dzongkhagRepo.findById(destinationDzongkhagId)
                .orElseThrow(() -> new IllegalArgumentException("Destination not found"));

        double lat1 = origin.getLatitude().doubleValue();
        double lng1 = origin.getLongitude().doubleValue();
        double lat2 = destination.getLatitude().doubleValue();
        double lng2 = destination.getLongitude().doubleValue();

        return dropPointRepo.findByIsActiveTrue().stream()
                .filter(dp -> {
                    double dpLat = dp.getLatitude().doubleValue();
                    double dpLng = dp.getLongitude().doubleValue();

                    // Must lie between origin and destination (not behind or beyond)
                    boolean withinSegment = isWithinSegment(lat1, lng1, lat2, lng2, dpLat, dpLng);

                    // Must be within buffer distance of the route line
                    double distToLine = perpendicularDistanceKm(
                            lat1, lng1, lat2, lng2, dpLat, dpLng);

                    return withinSegment && distToLine <= bufferKm;
                })
                .sorted(Comparator.comparingDouble(dp ->
                        HaversineUtil.distanceKm(
                                dp.getLatitude(), dp.getLongitude(),
                                origin.getLatitude(), origin.getLongitude())))
                .collect(Collectors.toList());
    }

    /**
     * Perpendicular distance from point P to line segment A→B (in km).
     * Uses cross-product of vectors in lat/lng space (flat-earth approximation —
     * accurate enough for Bhutan's distances).
     */
    private double perpendicularDistanceKm(double lat1, double lng1,
                                           double lat2, double lng2,
                                           double pLat, double pLng) {
        // Convert to approximate Cartesian (km)
        double x1 = 0, y1 = 0;
        double x2 = (lng2 - lng1) * Math.cos(Math.toRadians(lat1)) * 111.32;
        double y2 = (lat2 - lat1) * 110.574;
        double px = (pLng - lng1) * Math.cos(Math.toRadians(lat1)) * 111.32;
        double py = (pLat - lat1) * 110.574;

        double lineLen = Math.sqrt(x2 * x2 + y2 * y2);
        if (lineLen == 0) return HaversineUtil.distanceKm(lat1, lng1, pLat, pLng);

        // Cross product magnitude / line length = perpendicular distance
        return Math.abs(x2 * py - y2 * px) / lineLen;
    }

    /**
     * Checks if point P projects onto the segment A→B
     * (not behind A or beyond B).
     */
    private boolean isWithinSegment(double lat1, double lng1,
                                    double lat2, double lng2,
                                    double pLat, double pLng) {
        double x2 = (lng2 - lng1) * Math.cos(Math.toRadians(lat1)) * 111.32;
        double y2 = (lat2 - lat1) * 110.574;
        double px = (pLng - lng1) * Math.cos(Math.toRadians(lat1)) * 111.32;
        double py = (pLat - lat1) * 110.574;

        double dot = (px * x2 + py * y2) / (x2 * x2 + y2 * y2);
        return dot >= 0.0 && dot <= 1.0;  // 0=at origin, 1=at destination
    }
}
