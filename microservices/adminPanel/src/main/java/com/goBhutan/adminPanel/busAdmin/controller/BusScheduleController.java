package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.AppScheduleResponse;
import com.goBhutan.adminPanel.busAdmin.dto.GenerateScheduleRequest;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.service.BusScheduleService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
                adminUserId);

        return ResponseEntity.ok(ApiResponse.success("Schedules generated", schedules));
    }

    /* unimplemented
     *
     * Route-selected schedule generation is intentionally disabled for now.
     * The implemented flow generates schedules by busId and applies to all
     * active routes mapped to that bus.
     *
     * @PostMapping("/routes/generate")
     * public ResponseEntity<ApiResponse<List<Schedule>>> generateForRoutes(
     *         @Valid @RequestBody GenerateRouteScheduleRequest req) {
     *
     *     Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
     *     String adminUserId = principal.getSubject();
     *
     *     List<Schedule> schedules = scheduleService.generateSchedulesForRoutes(
     *             req.getRouteIds(),
     *             req.getStartDate(),
     *             req.getDays(),
     *             adminUserId);
     *
     *     return ResponseEntity.ok(ApiResponse.success("Schedules generated", schedules));
     * }
     */

    // ============= Read-Only Endpoints =============
    @GetMapping("/bus/{busId}")
    public ResponseEntity<ApiResponse<List<Schedule>>> getByBus(
            @PathVariable Long busId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        return ResponseEntity.ok(ApiResponse.success(
                scheduleService.getSchedulesByBus(busId, adminUserId, includeInactive)));
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<ApiResponse<List<Schedule>>> getByRoute(
            @PathVariable Long routeId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        return ResponseEntity.ok(ApiResponse.success(
                scheduleService.getSchedulesByRoute(routeId, adminUserId, includeInactive)));
    }

    // ============= App User Endpoints =============
    @GetMapping("/app/route/{routeId}")
    public ResponseEntity<ApiResponse<List<AppScheduleResponse>>> getAvailableSchedulesForApp(
            @PathVariable Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(
                scheduleService.getAvailableSchedulesForApp(routeId, date)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Schedule>> getById(@PathVariable Long id) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getScheduleById(id, adminUserId)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<Schedule>>> getByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        return ResponseEntity.ok(ApiResponse.success(
                scheduleService.getSchedulesByDateRange(adminUserId, start, end, includeInactive)));
    }

    // ============= Optional Actions =============
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Schedule>> toggle(@PathVariable Long id) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        return ResponseEntity
                .ok(ApiResponse.success("Status updated", scheduleService.toggleScheduleStatus(id, adminUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        String adminUserId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        scheduleService.deleteSchedule(id, adminUserId);
        return ResponseEntity.ok(ApiResponse.success("Schedule deactivated", "OK"));
    }
}
