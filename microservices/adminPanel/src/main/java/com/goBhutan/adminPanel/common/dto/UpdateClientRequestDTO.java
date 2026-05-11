package com.goBhutan.adminPanel.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClientRequestDTO {
    private String username;
    private String email;
    private int phoneNumber;
    private List<String> clients;
}