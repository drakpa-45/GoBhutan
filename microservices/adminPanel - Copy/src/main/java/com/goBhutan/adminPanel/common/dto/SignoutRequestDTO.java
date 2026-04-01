package com.goBhutan.adminPanel.common.dto;

import jakarta.validation.constraints.NotBlank;

public class SignoutRequestDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
