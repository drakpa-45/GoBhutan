package com.goBhutan.adminPanel.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppConfigResponse {

    private Long configId;

    private String configFor;

    private String configValue;

    private Boolean active;

    private String adminUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
