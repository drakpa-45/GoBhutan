package com.goBhutan.adminPanel.taxi.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VehicleImageResponse {
    private Long   id;
    private String imagePath;          // /uploads/taxi/101_0_uuid.jpg
    private String originalFilename;
    private Integer displayOrder;
    private LocalDateTime uploadedAt;
}
