package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManifestItem {
    private Integer seatNumber;
    private String seatLabel;
    private String cid;
    private String mobile;
    private String email;
    private String status;
}
