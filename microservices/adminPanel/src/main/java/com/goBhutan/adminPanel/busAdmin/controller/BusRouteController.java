package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.RouteRegistrationRequest;
import com.goBhutan.adminPanel.busAdmin.entity.Route;
import com.goBhutan.adminPanel.busAdmin.service.BusRouteService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BusRouteController {

    @Autowired
    private BusRouteService routeService;


    @PostMapping
    public ResponseEntity<ApiResponse<Route>> registerRoute(@Valid @RequestBody RouteRegistrationRequest routeRegistrationRequest,
                                                            HttpServletRequest request) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            Route route = routeService.registerRoute(routeRegistrationRequest, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Route registered successfully", route));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Route>>> getRoutes(HttpServletRequest request) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            List<Route> routes = routeService.getRoutesByOwner(adminUserId);
            return ResponseEntity.ok(ApiResponse.success(routes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/bus/{busId}")
    public ResponseEntity<ApiResponse<List<Route>>> getRoutesByBus(@PathVariable Long busId, HttpServletRequest request) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            List<Route> routes = routeService.getRoutesByBus(busId);
            return ResponseEntity.ok(ApiResponse.success(routes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Route>> getRoute(@PathVariable Long id, HttpServletRequest request) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            Route route = routeService.getRouteById(id, adminUserId);
            return ResponseEntity.ok(ApiResponse.success(route));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Route>> updateRoute(@PathVariable Long id,
                                                          @Valid @RequestBody RouteRegistrationRequest routeRegistrationRequest,
                                                          HttpServletRequest request) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            Route route = routeService.updateRoute(id, routeRegistrationRequest, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Route updated successfully", route));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRoute(@PathVariable Long id, HttpServletRequest request) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            routeService.deleteRoute(id, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Route deleted successfully", "Route deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

}
