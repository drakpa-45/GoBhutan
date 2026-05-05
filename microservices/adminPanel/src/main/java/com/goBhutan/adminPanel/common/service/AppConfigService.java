package com.goBhutan.adminPanel.common.service;

import com.goBhutan.adminPanel.common.dto.AppConfigRequest;
import com.goBhutan.adminPanel.common.dto.AppConfigResponse;
import com.goBhutan.adminPanel.common.dto.AppConfigUpdateRequest;
import com.goBhutan.adminPanel.common.entity.AppConfig;
import com.goBhutan.adminPanel.common.repository.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AppConfigService {

    private final AppConfigRepository appConfigRepository;

    public AppConfigResponse create(AppConfigRequest request, String adminUserId) {
        String configFor = normalizeRequired(request.getConfigFor(), "CONFIG_FOR");
        String configValue = normalizeRequired(request.getConfigValue(), "CONFIG_VALUE");
        String normalizedAdminUserId = normalizeRequired(adminUserId, "admin_user_id");

        AppConfig config = new AppConfig();
        config.setConfigFor(configFor);
        config.setConfigValue(configValue);
        config.setActive(request.getActive() == null ? true : request.getActive());
        config.setAdminUserId(normalizedAdminUserId);

        return toResponse(appConfigRepository.save(config));
    }

    public AppConfigResponse update(Long id, AppConfigUpdateRequest request) {
        String configValue = normalizeRequired(request.getConfigValue(), "CONFIG_VALUE");

        AppConfig config = appConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Config not found"));

        config.setConfigValue(configValue);
        if (request.getActive() != null) {
            config.setActive(request.getActive());
        }

        return toResponse(appConfigRepository.save(config));
    }

    @Transactional(readOnly = true)
    public AppConfigResponse getById(Long id) {
        return appConfigRepository.findById(id)
                .filter(config -> Boolean.TRUE.equals(config.getActive()))
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Active config not found"));
    }

    @Transactional(readOnly = true)
    public List<AppConfigResponse> getByConfigFor(String configFor) {
        String normalizedConfigFor = normalizeRequired(configFor, "CONFIG_FOR");
        return appConfigRepository.findByConfigForAndActiveTrueOrderByConfigIdAsc(normalizedConfigFor).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public String getFirstActiveConfigValue(String configFor) {
        String normalizedConfigFor = normalizeRequired(configFor, "CONFIG_FOR");
        return appConfigRepository.findByConfigForAndActiveTrueOrderByConfigIdAsc(normalizedConfigFor).stream()
                .map(AppConfig::getConfigValue)
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(String::trim)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Active config value not found for CONFIG_FOR: " + normalizedConfigFor));
    }

    @Transactional(readOnly = true)
    public List<AppConfigResponse> getAll() {
        List<AppConfig> configs = appConfigRepository.findByActiveTrueOrderByConfigForAscConfigIdAsc();

        return configs.stream()
                .map(this::toResponse)
                .toList();
    }

    private AppConfigResponse toResponse(AppConfig config) {
        return AppConfigResponse.builder()
                .configId(config.getConfigId())
                .configFor(config.getConfigFor())
                .configValue(config.getConfigValue())
                .active(config.getActive())
                .adminUserId(config.getAdminUserId())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(fieldName + " is required");
        }
        return value.trim();
    }
}
