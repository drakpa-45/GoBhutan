package com.goBhutan.adminPanel.taxi.controller;

import com.goBhutan.adminPanel.taxi.dto.request.TaxiDriverRequest;
import com.goBhutan.adminPanel.taxi.dto.response.TaxiDriverResponse;
import com.goBhutan.adminPanel.taxi.service.TaxiDriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/taxi/driver")
@RequiredArgsConstructor
public class TaxiDriverController {

    private final TaxiDriverService service;

    /**
     * POST /taxi/driver/register
     * multipart/form-data
     *
     * Form fields:
     *   vehicleMake, vehicleModel, vehicleColor,
     *   totalSeats, licenseNumber, registrationNumber
     *
     * File part (multiple files, same key):
     *   images[]  →  any number of vehicle photos (max 10, 5 MB each, JPG/PNG/WEBP)
     *
     * Example curl:
     *   curl -X POST /taxi/driver/register \
     *     -F "vehicleMake=Toyota" -F "vehicleModel=HiAce" \
     *     -F "vehicleColor=White" -F "totalSeats=10" \
     *     -F "licenseNumber=BT-DL-2021-0042" \
     *     -F "registrationNumber=BP-1234A" \
     *     -F "images[]=@front.jpg" \
     *     -F "images[]=@side.jpg" \
     *     -F "images[]=@interior.jpg"
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TaxiDriverResponse> register(
            @Valid @ModelAttribute TaxiDriverRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        return ResponseEntity.ok(service.register(extractDriverId(), request, images));
    }

    /**
     * GET /taxi/driver/getVechileDetails
     * Returns vehicle details + all uploaded images in display order.
     */
    @GetMapping("/getVechileDetails")
    public ResponseEntity<TaxiDriverResponse> getMyVehicle() {
        return ResponseEntity.ok(service.getMyRegistration(extractDriverId()));
    }

    /**
     * PUT /taxi/driver/updateVehicleDetails
     * Updates vehicle details and APPENDS new images (existing images are kept).
     * To remove a specific image use DELETE /taxi/driver/me/images/{imageId}.
     *
     * images[]  →  new photos to add (optional)
     */
    @PutMapping(value = "/updateVehicleDetails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TaxiDriverResponse> update(
            @Valid @ModelAttribute TaxiDriverRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(service.update(extractDriverId(), request, images));
    }

    /**
     * DELETE /taxi/driver/vechile/images/{imageId}
     * Driver removes a specific photo by its id.
     * File is deleted from disk and display_order of remaining images is adjusted.
     */
    @DeleteMapping("/vechile/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long imageId) {

        service.deleteImage(extractDriverId(), imageId);
        return ResponseEntity.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String extractDriverId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId;

        if (principal instanceof Jwt jwt) {
            userId = jwt.getSubject(); // Keycloak "sub" claim
        } else if (principal instanceof String str) {
            userId = str; // fallback if principal is String
        } else {
            throw new RuntimeException("Unsupported principal type: " + principal.getClass());
        }
        return userId;
    }
}
