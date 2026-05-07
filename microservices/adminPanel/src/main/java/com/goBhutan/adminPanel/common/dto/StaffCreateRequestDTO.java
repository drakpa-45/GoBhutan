package com.goBhutan.adminPanel.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffCreateRequestDTO {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private Integer phoneNumber;
    private String client;       // e.g. "hotel"
    private String entityId;     // e.g. specific hotelId from tbl_ht_hotels
    private String entityType;   // e.g. "hotel"
}