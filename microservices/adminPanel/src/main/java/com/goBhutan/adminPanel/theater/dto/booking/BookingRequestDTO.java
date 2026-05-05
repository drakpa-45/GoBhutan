package com.goBhutan.adminPanel.theater.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {

    @NotNull
    private Long screeningId;

    @NotEmpty
    private List<TicketRequest> tickets;
    private String userId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketRequest {
        @NotNull
        private Long seatId;

        @NotBlank
        private String customerName;

        @NotBlank
        private String cidOrPassport;

        @NotBlank
        private String phoneNumber;

        private String email;

    }
}
