package com.goBhutan.adminPanel.taxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Haversine formula — straight-line distance between two GPS coordinates.
 * Accurate to within ~0.5% for distances up to 500 km.
 * Used for: nearest driver matching, ETA estimation, trip distance verification.
 */
public final class HaversineUtil {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private HaversineUtil() {}

    /**
     * Returns distance in km between two lat/lng points.
     */
    public static double distanceKm(double lat1, double lng1,
                                     double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static double distanceKm(BigDecimal lat1, BigDecimal lng1,
                                     BigDecimal lat2, BigDecimal lng2) {
        return distanceKm(lat1.doubleValue(), lng1.doubleValue(),
                          lat2.doubleValue(), lng2.doubleValue());
    }

    /**
     * Rough ETA in minutes.
     * Uses 30 km/h average for Bhutan mountain roads (conservative).
     * Replace with Google Maps Directions API for production accuracy.
     */
    public static int etaMinutes(double distanceKm) {
        double avgSpeedKmh = 30.0;
        return (int) Math.ceil((distanceKm / avgSpeedKmh) * 60);
    }

    /**
     * Bounding box for a radius search.
     * Returns [latMin, latMax, lngMin, lngMax].
     * 1 degree lat ≈ 111 km; 1 degree lng ≈ 111 * cos(lat) km.
     */
    public static double[] boundingBox(double lat, double lng, double radiusKm) {
        double latDelta = radiusKm / 111.0;
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));
        return new double[]{
            lat - latDelta,   // latMin
            lat + latDelta,   // latMax
            lng - lngDelta,   // lngMin
            lng + lngDelta    // lngMax
        };
    }

    public static BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(7, RoundingMode.HALF_UP);
    }
}
