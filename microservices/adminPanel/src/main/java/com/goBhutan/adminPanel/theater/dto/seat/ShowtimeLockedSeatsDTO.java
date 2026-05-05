// ShowtimeLockedSeatsDTO.java  — for the GET endpoint
package com.goBhutan.adminPanel.theater.dto.seat;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data @Builder
public class ShowtimeLockedSeatsDTO {

    private Long screenId;
    private Long hallId;             // filter by hall

    // Grouped by class so the seat map can color per class
    private Map<String, List<Long>> lockedSeatIdsByClass;
    // e.g. { "VIP": [1, 2], "Economy": [5, 9] }
}