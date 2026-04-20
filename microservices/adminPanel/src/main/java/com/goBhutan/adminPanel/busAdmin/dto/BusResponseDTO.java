package com.goBhutan.adminPanel.busAdmin.dto;

import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusResponseDTO {
    private Long id;
    private String busNumber;
    private String busName;
    private String busType;
    private Integer totalSeats;
    private String description;
    private String amenities;
    private String adminUserId;
    private String layoutType;

    //private Set<DayOfWeek> operatingDays;
    private List<BusSeatConfig> seats;
   // private List<BusRouteResponse> busRoutes;
    //private List<ScheduleRequest> schedules;
}
