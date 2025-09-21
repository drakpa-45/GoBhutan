package com.goBhutan.adminPanel.busAdmin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goBhutan.adminPanel.busAdmin.dto.BusRegistrationRequest;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.service.BusService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buses")
public class BusRegistrationController {
    private static final Logger logger = LoggerFactory.getLogger(BusRegistrationController.class);
    @Autowired
    private BusService busService;

    @PostMapping
    public ResponseEntity<ApiResponse<Bus>> registerBus(@Valid @RequestBody BusRegistrationRequest busRegistrationRequest, HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            Bus bus = busService.registerBus(busRegistrationRequest, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Bus registered successfully", bus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Bus>>> getBuses(HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            List<Bus> buses = busService.getBusesByOwner(adminUserId);
            return ResponseEntity.ok(ApiResponse.success(buses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Bus>> getBus(@PathVariable Long id, HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            Bus bus = busService.getBusById(id, adminUserId);
            return ResponseEntity.ok(ApiResponse.success(bus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Bus>> updateBus(@PathVariable Long id,
                                                      @Valid @RequestBody BusRegistrationRequest busRegistrationRequest,
                                                      HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            Bus bus = busService.updateBus(id, busRegistrationRequest, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Bus updated successfully", bus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBus(@PathVariable Long id, HttpServletRequest request) {
        try {
            String adminUserId = "LLL";
            busService.deleteBus(id, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Bus deleted successfully", "Bus deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping(value = "/healthCheck", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String healthCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        String token = resolveBearerToken(request);
        logger.debug("Extracted token: {}", token);

        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return objectMapper.writeValueAsString(
                    Map.of("status", "UNAUTHORIZED", "message", "Missing Bearer token")
            );
        }
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            return objectMapper.writeValueAsString(
                    Map.of("status", "OK", "message", "Token received", "tokenSnippet", token.substring(0, Math.min(16, token.length())) + "…")
            );
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return objectMapper.writeValueAsString(
                    Map.of("status", "UNAUTHORIZED", "message", "Invalid or expired token")
            );
        }
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null) auth = request.getHeader("authorization");
        if (auth != null) {
            auth = auth.trim();
            if (auth.toLowerCase().startsWith("bearer ")) {
                return auth.substring(7).trim();
            }
        }
        String qp = request.getParameter("access_token");
        if (qp != null && !qp.trim().isEmpty()) return qp.trim();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("access_token".equalsIgnoreCase(c.getName()) ||
                        "Authorization".equalsIgnoreCase(c.getName())) {
                    String v = c.getValue();
                    if (v != null) {
                        v = v.trim();
                        if (v.toLowerCase().startsWith("bearer ")) v = v.substring(7).trim();
                        if (!v.isEmpty()) return v;
                    }
                }
            }
        }
        return null;
    }

}