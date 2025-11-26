package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.GenerateScheduleRequest;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.service.BusScheduleService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
public class BusScheduleController {

    private final BusScheduleService scheduleService;

    // ============= Generate Schedules =============
    @PostMapping("/bus/generate")
    public ResponseEntity<ApiResponse<List<Schedule>>> generate(@Valid @RequestBody GenerateScheduleRequest req) {

        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String adminUserId = principal.getSubject();

        List<Schedule> schedules = scheduleService.generateSchedules(
                req.getBusId(),
                req.getStartDate(),
                req.getDays(),
                adminUserId
        );

        return ResponseEntity.ok(ApiResponse.success("Schedules generated", schedules));
    }

    // ============= Read-Only Endpoints =============
    @GetMapping("/bus/{busId}")
    public ResponseEntity<ApiResponse<List<Schedule>>> getByBus(@PathVariable Long busId) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getSchedulesByBus(busId, adminUserId)));
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<ApiResponse<List<Schedule>>> getByRoute(@PathVariable Long routeId) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getSchedulesByRoute(routeId, adminUserId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Schedule>> getById(@PathVariable Long id) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getScheduleById(id, adminUserId)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<Schedule>>> getByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getSchedulesByDateRange(adminUserId, start, end)));
    }

    // ============= Optional Actions =============
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Schedule>> toggle(@PathVariable Long id) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        return ResponseEntity.ok(ApiResponse.success("Status updated", scheduleService.toggleScheduleStatus(id, adminUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        scheduleService.deleteSchedule(id, adminUserId);
        return ResponseEntity.ok(ApiResponse.success("Deleted", "OK"));
    }
}
