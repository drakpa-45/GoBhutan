package com.goBhutan.adminPanel.common.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.common.dto.AppConfigRequest;
import com.goBhutan.adminPanel.common.dto.AppConfigResponse;
import com.goBhutan.adminPanel.common.dto.AppConfigUpdateRequest;
import com.goBhutan.adminPanel.common.service.AppConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AppConfigController {

    private final AppConfigService appConfigService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppConfigResponse>> create(@Valid @RequestBody AppConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Config created successfully",
                appConfigService.create(request, currentUserId())
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppConfigResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AppConfigUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Config updated successfully",
                appConfigService.update(id, request)
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getByQueryParam(
            @RequestParam Map<String, String> params
    ) {
        String configFor = firstNonBlank(params.get("configFor"));
        Object response = configFor == null
                ? appConfigService.getAll()
                : appConfigService.getByConfigFor(configFor);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/config-for/{configFor}")
    public ResponseEntity<ApiResponse<List<AppConfigResponse>>> getByConfigFor(@PathVariable String configFor) {
        return ResponseEntity.ok(ApiResponse.success(appConfigService.getByConfigFor(configFor)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppConfigResponse>> getByPath(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(appConfigService.getById(id)));
    }

    private String currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }
}
