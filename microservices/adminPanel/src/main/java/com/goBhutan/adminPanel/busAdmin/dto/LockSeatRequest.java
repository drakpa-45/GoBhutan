package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.Data;

import java.util.List;

@Data
public class LockSeatRequest {
    private Long scheduleId;
    private List<Integer> seatNumbers;
    private List<String> seatLabels;
    // private String applicantCid;
    private List<String> applicantCids;
    private List<String> applicantNames;
    private String applicantMobile;
    private String applicantEmail;
    private String status;
}
