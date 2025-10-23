package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.RouteRegistrationRequest;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.Route;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusRouteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class BusRouteService {

    @Autowired
    private BusRouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    public Route registerRoute(RouteRegistrationRequest request, String adminUserId) {
        Bus bus = busRepository.findByIdAndAdminUserId(request.getBusId(), adminUserId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        Route route = new Route();
        route.setSource(request.getSource());
        route.setDestination(request.getDestination());
        route.setDistance(request.getDistance());
        route.setBaseFare(request.getBaseFare());
        route.setEstimatedDuration(request.getEstimatedDuration());
        route.setBus(bus);

        return routeRepository.save(route);
    }

    public List<Route> getRoutesByOwner(String adminUserId) {
        return routeRepository.findByBusAdminUserId(adminUserId);
    }

    public List<Route> getRoutesByBus(Long busId) {
        return routeRepository.findByBusId(busId);
    }

    public Route getRouteById(Long routeId, String adminUserId) {
        return routeRepository.findByIdAndBusAdminUserId(routeId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
    }

    public Route updateRoute(Long routeId, RouteRegistrationRequest request, String adminUserId) {
        Route route = getRouteById(routeId, adminUserId);

        // Verify the bus belongs to the owner
        busRepository.findByIdAndAdminUserId(request.getBusId(), adminUserId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        route.setSource(request.getSource());
        route.setDestination(request.getDestination());
        route.setDistance(request.getDistance());
        route.setBaseFare(request.getBaseFare());
        route.setEstimatedDuration(request.getEstimatedDuration());

        return routeRepository.save(route);
    }

    public void deleteRoute(Long routeId, String adminUserId) {
        Route route = getRouteById(routeId, adminUserId);
        routeRepository.delete(route);
    }

}
