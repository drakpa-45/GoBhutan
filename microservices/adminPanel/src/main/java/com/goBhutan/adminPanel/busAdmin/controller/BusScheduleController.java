package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.ScheduleRequest;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.service.BusScheduleService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BusScheduleController {

    @Autowired
    private BusScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ApiResponse<Schedule>> createSchedule(@Valid @RequestBody ScheduleRequest scheduleRequest,
                                                                HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            Schedule schedule = scheduleService.createSchedule(scheduleRequest, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Schedule created successfully", schedule));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Schedule>>> getSchedules(HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            List<Schedule> schedules = scheduleService.getSchedulesByOwner(adminUserId);
            return ResponseEntity.ok(ApiResponse.success(schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/bus/{busId}")
    public ResponseEntity<ApiResponse<List<Schedule>>> getSchedulesByBus(@PathVariable Long busId, HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            List<Schedule> schedules = scheduleService.getSchedulesByBus(busId,adminUserId);
            return ResponseEntity.ok(ApiResponse.success(schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<ApiResponse<List<Schedule>>> getSchedulesByRoute(@PathVariable Long routeId, HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            List<Schedule> schedules = scheduleService.getSchedulesByRoute(routeId,adminUserId);
            return ResponseEntity.ok(ApiResponse.success(schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<Schedule>>> getSchedulesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            List<Schedule> schedules = scheduleService.getSchedulesByDateRange(adminUserId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Schedule>> getSchedule(@PathVariable Long id, HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            Schedule schedule = scheduleService.getScheduleById(id, adminUserId);
            return ResponseEntity.ok(ApiResponse.success(schedule));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Schedule>> updateSchedule(@PathVariable Long id,
                                                                @Valid @RequestBody ScheduleRequest scheduleRequest,
                                                                HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            Schedule schedule = scheduleService.updateSchedule(id, scheduleRequest, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Schedule updated successfully", schedule));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Schedule>> toggleScheduleStatus(@PathVariable Long id, HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            Schedule schedule = scheduleService.toggleScheduleStatus(id, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Schedule status updated", schedule));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSchedule(@PathVariable Long id, HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            scheduleService.deleteSchedule(id, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Schedule deleted successfully", "Schedule deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
