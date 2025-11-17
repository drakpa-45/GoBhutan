package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.RevenueReport;
import com.goBhutan.adminPanel.busAdmin.entity.Bookings;
import com.goBhutan.adminPanel.busAdmin.service.BusBookingService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BusBookingController {
    @Autowired
    private BusBookingService busBookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Bookings>>> getBookings(HttpServletRequest request) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            List<Bookings> bookings = busBookingService.findByBusAdminUserId(adminUserId);
            return ResponseEntity.ok(ApiResponse.success(bookings));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<ApiResponse<List<Bookings>>> getBookingsBySchedule(@PathVariable Long scheduleId, HttpServletRequest request) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            List<Bookings> bookings = busBookingService.getBookingsBySchedule(scheduleId);
            return ResponseEntity.ok(ApiResponse.success(bookings));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueReport>> getRevenueReport(HttpServletRequest request) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            RevenueReport report = busBookingService.getRevenueReport(adminUserId);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/revenue/date-range")
    public ResponseEntity<ApiResponse<RevenueReport>> getRevenueReportByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest request) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            RevenueReport report = busBookingService.getRevenueReportByDateRange(adminUserId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }



}
