package com.goBhutan.adminPanel.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDTO {
    private String username;
    private String otp;
    private String newPassword;
}
