package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.BusRouteRequest;
import com.goBhutan.adminPanel.busAdmin.dto.BusRouteResponse;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusRoute;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusRouteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BusRouteServiceNew {

    private final BusRepository busRepository;
    private final BusRouteRepository busRouteRepository;

    // ========================= CREATE ===============================
    public BusRouteResponse createRoute(BusRouteRequest req, String adminUserId) {

        Bus bus = busRepository.findByIdAndAdminUserId(req.getBusId(), adminUserId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        // Prevent duplicate
        boolean exists = busRouteRepository.existsByBusAndDepartureTimeAndSourceAndDestinationAndActiveTrue(
                bus, req.getDepartureTime(), req.getSource(), req.getDestination());

        if (exists) {
            throw new RuntimeException("Route already exists for this bus at the specified departure time.");
        }

        BusRoute br = new BusRoute();
        br.setBus(bus);
        br.setSource(req.getSource());
        br.setDestination(req.getDestination());
        br.setDistance(req.getDistance());
        br.setBaseFare(req.getBaseFare());
        br.setEstimatedDuration(req.getEstimatedDuration());
        br.setDepartureTime(req.getDepartureTime());
        br.setCustomFare(req.getCustomFare());
        br.setActive(req.getActive() != null ? req.getActive() : true);

        return toResponse(busRouteRepository.save(br));
    }

    // ========================= UPDATE ===============================

    public BusRouteResponse updateRoute(Long id, BusRouteRequest req, String adminUserId) {

        BusRoute br = busRouteRepository.findByIdAndBus_AdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (!br.getBus().getId().equals(req.getBusId())) {
            throw new RuntimeException("Route cannot be moved to a different bus");
        }

        boolean willBeActive = req.getActive() != null ? req.getActive() : br.getActive();
        if (willBeActive && busRouteRepository.existsByBusAndDepartureTimeAndSourceAndDestinationAndActiveTrueAndIdNot(
                br.getBus(),
                req.getDepartureTime(),
                req.getSource(),
                req.getDestination(),
                br.getId()
        )) {
            throw new RuntimeException("Route already exists for this bus at the specified departure time.");
        }

        br.setSource(req.getSource());
        br.setDestination(req.getDestination());
        br.setDistance(req.getDistance());
        br.setBaseFare(req.getBaseFare());
        br.setEstimatedDuration(req.getEstimatedDuration());
        br.setDepartureTime(req.getDepartureTime());
        br.setCustomFare(req.getCustomFare());
        br.setActive(req.getActive() != null ? req.getActive() : br.getActive());

        return toResponse(busRouteRepository.save(br));
    }

    // ========================= SOFT DELETE ===========================

    public void softDeleteRoute(Long id, String adminUserId) {
        BusRoute route = busRouteRepository.findByIdAndBus_AdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        route.setActive(false);
        busRouteRepository.save(route);
    }

    // ========================= GET ONE ===============================

    public BusRouteResponse getRoute(Long id, String adminUserId) {
        BusRoute route = busRouteRepository.findByIdAndBus_AdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        return toResponse(route);
    }

    // ========================= GET ALL FOR BUS =======================

    public List<BusRouteResponse> getRoutesByBus(Long busId, String adminUserId) {

        return busRouteRepository.findByBus_IdAndBus_AdminUserIdAndActiveTrue(busId, adminUserId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ========================= MAPPER ================================
    private BusRouteResponse toResponse(BusRoute br) {
        return new BusRouteResponse(
                br.getId(),
                br.getBus().getId(),
                br.getBus().getBusNumber(),
                br.getSource(),
                br.getDestination(),
                br.getDistance(),
                br.getBaseFare(),
                br.getFinalFare(),
                br.getEstimatedDuration(),
                br.getDepartureTime(),
                br.getActive()
        );
    }
}
