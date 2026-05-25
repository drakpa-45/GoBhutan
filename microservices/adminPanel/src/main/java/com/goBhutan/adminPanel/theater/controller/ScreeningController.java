package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.theater.dto.screening.ScreeningDTO;
import com.goBhutan.adminPanel.theater.entity.Screening;
import com.goBhutan.adminPanel.theater.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/screenings")
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;

/*    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ScreeningDTO>> createScreening(@RequestBody ScreeningDTO dto,
         @RequestPart(required = false, name = "posterImages") MultipartFile posterImages) {
        return ResponseEntity.ok(ApiResponse.success("Screening created", screeningService.createScreening(dto,posterImages)));
    }*/


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ScreeningDTO>> createScreening(
            @RequestPart("dto") ScreeningDTO dto,
            @RequestPart(value = "posterImages", required = false) MultipartFile posterImages
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Screening created",
                        screeningService.createScreening(dto, posterImages)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScreeningDTO>> getScreening(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(screeningService.getScreening(id)));
    }

    @GetMapping("/hall/{hallId}")
    public ResponseEntity<ApiResponse<List<ScreeningDTO>>> getScreeningsByHall(@PathVariable Long hallId) {
        return ResponseEntity.ok(ApiResponse.success(screeningService.getScreeningsByHall(hallId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScreeningDTO>> updateScreening(@PathVariable Long id, @RequestBody ScreeningDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Screening updated", screeningService.updateScreening(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScreening(@PathVariable Long id) {
        screeningService.deleteScreening(id);
        return ResponseEntity.ok(ApiResponse.success("Screening deleted", null));
    }
}
