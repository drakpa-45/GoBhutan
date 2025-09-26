package com.goBhutan.adminPanel.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDTO {
    private String userId;      // DB UUID
    private String keycloakId;  // KC UUID
    private String username;
    private String email;
    private List<String> clients;
}
