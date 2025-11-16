package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.BusRouteMapRequest;
import com.goBhutan.adminPanel.busAdmin.dto.BusRouteMapResponse;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusRouteMap;
import com.goBhutan.adminPanel.busAdmin.entity.Route;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusRouteMapRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusRouteRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@Transactional
public class BusRouteMapService {
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private BusRouteRepository routeRepository;
    @Autowired
    private BusRouteMapRepository busRouteMapRepository;

    private static final Logger log = LoggerFactory.getLogger(BusRouteMapService.class);

    public BusRouteMapResponse addMapping(BusRouteMapRequest req, String adminUserId) {
        Bus bus = busRepository.findByIdAndAdminUserId(req.getBusId(), adminUserId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        Route route = routeRepository.findByIdAndBus_AdminUserId(req.getRouteId(), adminUserId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        // Prevent duplicates
        boolean exists = busRouteMapRepository.findByBusAndActiveTrue(bus)
                .stream()
                .anyMatch(m -> m.getRoute().getId().equals(route.getId()) &&
                        m.getDepartureTime().equals(req.getDepartureTime()));

        if (exists) throw new RuntimeException("Mapping already exists for this route and departure time.");

        BusRouteMap map = new BusRouteMap();
        map.setBus(bus);
        map.setRoute(route);
        map.setDepartureTime(req.getDepartureTime());
        map.setCustomFare(req.getCustomFare());
        map.setEstimatedDuration(req.getEstimatedDuration());
        map.setActive(req.getActive() != null ? req.getActive() : true);

        BusRouteMap saved = busRouteMapRepository.save(map);
        log.info("✅ Added mapping: Bus {} → Route {} at {}", bus.getBusNumber(), route.getSource(), req.getDepartureTime());
        return toResponse(saved);
    }

    /**
     * ✅ Update existing mapping
     */
    public BusRouteMapResponse updateMapping(Long id, BusRouteMapRequest req, String adminUserId) {
        BusRouteMap map = busRouteMapRepository.findByIdAndBus_AdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Mapping not found"));

        if (!map.getBus().getId().equals(req.getBusId()))
            throw new RuntimeException("Mapping does not belong to the specified bus.");

        Route route = routeRepository.findByIdAndBus_AdminUserId(req.getRouteId(), adminUserId)
                .orElseThrow(() -> new RuntimeException("Route not found."));

        map.setRoute(route);
        map.setDepartureTime(req.getDepartureTime());
        map.setCustomFare(req.getCustomFare());
        map.setEstimatedDuration(req.getEstimatedDuration());
        map.setActive(req.getActive());

        return toResponse(busRouteMapRepository.save(map));
    }

    /**
     * ✅ Get all mappings for a specific bus
     */
    public List<BusRouteMapResponse> getMappingsByBus(Long busId, String adminUserId) {
        return busRouteMapRepository.findByBus_IdAndBus_AdminUserId(busId, adminUserId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Delete (soft or hard)
     */
    public void deleteMapping(Long id, String adminUserId) {
        BusRouteMap map = busRouteMapRepository.findByIdAndBus_AdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Mapping not found."));
        busRouteMapRepository.delete(map);
    }

    /**
     * ✅ Convert entity to response DTO
     */
    private BusRouteMapResponse toResponse(BusRouteMap map) {
        return new BusRouteMapResponse(
                map.getId(),
                map.getBus().getId(),
                map.getBus().getBusNumber(),
                map.getRoute().getId(),
                map.getRoute().getSource(),
                map.getRoute().getDestination(),
                map.getDepartureTime(),
                Optional.ofNullable(map.getCustomFare()).orElse(map.getRoute().getBaseFare()),
                Optional.ofNullable(map.getEstimatedDuration()).orElse(map.getRoute().getEstimatedDuration()),
                map.getActive()
        );
    }
}
