package com.goBhutan.adminPanel.notification.dto;

import com.goBhutan.adminPanel.notification.enums.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterDeviceRequest {
    @NotBlank private String firebaseToken;
    @NotBlank private String deviceId;
    @NotNull private Platform platform;
    @NotBlank private String appName;
    @NotBlank private String appVersion;
    private String deviceModel;
    private Boolean permissionGranted = true;
}
