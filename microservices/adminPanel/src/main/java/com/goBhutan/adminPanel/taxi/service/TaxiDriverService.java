package com.goBhutan.adminPanel.taxi.service;

import com.goBhutan.adminPanel.taxi.dto.request.TaxiDriverRequest;
import com.goBhutan.adminPanel.taxi.dto.response.TaxiDriverResponse;
import com.goBhutan.adminPanel.taxi.dto.response.VehicleImageResponse;
import com.goBhutan.adminPanel.taxi.entity.TaxiDriver;
import com.goBhutan.adminPanel.taxi.entity.TaxiDriverImage;
import com.goBhutan.adminPanel.taxi.repository.TaxiDriverImageRepository;
import com.goBhutan.adminPanel.taxi.repository.TaxiDriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TaxiDriverService {

    private final TaxiDriverRepository      repo;
    private final TaxiDriverImageRepository imageRepo;
    private final TaxiImageStorageService   imageStorage;

    private static final int MAX_IMAGES = 10;

    // ── Register ──────────────────────────────────────────────────────────────

    @Transactional
    public TaxiDriverResponse register(String driverId, TaxiDriverRequest req,
                                        List<MultipartFile> images) {
        if (repo.findByDriverId(driverId).isPresent())
            throw new IllegalStateException(
                    "Vehicle already registered. Use PUT /taxi/driver/me to update.");

        if (repo.existsByLicenseNumber(req.getLicenseNumber().toUpperCase().trim()))
            throw new IllegalStateException("License number already registered.");

        if (repo.existsByRegistrationNumber(req.getRegistrationNumber().toUpperCase().trim()))
            throw new IllegalStateException("Registration number already registered.");

        TaxiDriver entity = TaxiDriver.builder()
                .driverId(driverId)
                .vehicleMake(req.getVehicleMake().trim())
                .vehicleModel(req.getVehicleModel().trim())
                .vehicleColor(req.getVehicleColor().trim())
                .totalSeats(req.getTotalSeats())
                .licenseNumber(req.getLicenseNumber().toUpperCase().trim())
                .registrationNumber(req.getRegistrationNumber().trim())
                .driverName(req.getDriverName().trim())
                .phoneNumber(req.getPhoneNumber())
                .isOnline(false)
                .build();

        TaxiDriver saved = repo.save(entity);

        // Save each uploaded image as a separate row
        saveImages(saved, images, 0);

        return toResponse(saved);
    }

    // ── Get own registration ──────────────────────────────────────────────────

    public TaxiDriverResponse getMyRegistration(String driverId) {
        return repo.findByDriverId(driverId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("No vehicle registered yet."));
    }

    // ── Update vehicle details ────────────────────────────────────────────────

    @Transactional
    public TaxiDriverResponse update(String driverId, TaxiDriverRequest req,
                                      List<MultipartFile> newImages) {
        TaxiDriver entity = repo.findByDriverId(driverId)
                .orElseThrow(() -> new IllegalArgumentException("No vehicle registered yet."));

        entity.setVehicleMake(req.getVehicleMake().trim());
        entity.setVehicleModel(req.getVehicleModel().trim());
        entity.setVehicleColor(req.getVehicleColor().trim());
        entity.setTotalSeats(req.getTotalSeats());
        entity.setLicenseNumber(req.getLicenseNumber().toUpperCase().trim());
        entity.setRegistrationNumber(req.getRegistrationNumber().toUpperCase().trim());
        entity.setDriverName(req.getDriverName().trim());
        entity.setPhoneNumber(req.getPhoneNumber());

        // Append new images after existing ones (don't replace unless driver deletes)
        if (newImages != null && !newImages.isEmpty()) {
            int currentCount = imageRepo.countByTaxiDriverId(entity.getId());
            int totalAfter   = currentCount + newImages.size();
            if (totalAfter > MAX_IMAGES)
                throw new IllegalStateException(
                        "Cannot exceed " + MAX_IMAGES + " images. " +
                        "You have " + currentCount + ", trying to add " + newImages.size() + ".");
            saveImages(entity, newImages, currentCount);
        }

        return toResponse(repo.save(entity));
    }

    // ── Delete a single image ─────────────────────────────────────────────────

    @Transactional
    public void deleteImage(String driverId, Long imageId) {
        TaxiDriver entity = repo.findByDriverId(driverId)
                .orElseThrow(() -> new IllegalArgumentException("No vehicle registered yet."));

        TaxiDriverImage image = imageRepo
                .findByIdAndTaxiDriverId(imageId, entity.getId())
                .orElseThrow(() -> new IllegalArgumentException("Image not found."));

        int deletedOrder = image.getDisplayOrder();

        // Delete from disk then DB
        imageStorage.delete(image.getImagePath());
        imageRepo.delete(image);

        // Shift display_order of subsequent images down by 1
        imageRepo.shiftOrderAfterDelete(entity.getId(), deletedOrder);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Saves a list of files as TaxiDriverImage rows.
     * startIndex = current image count so display_order continues from where we left off.
     */
    private void saveImages(TaxiDriver entity, List<MultipartFile> files, int startIndex) {
        if (files == null || files.isEmpty()) return;

        List<MultipartFile> nonEmpty = files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .collect(Collectors.toList());

        if (nonEmpty.isEmpty()) return;

        List<TaxiDriverImage> imageEntities = IntStream.range(0, nonEmpty.size())
                .mapToObj(i -> {
                    MultipartFile file  = nonEmpty.get(i);
                    int           order = startIndex + i;
                    String        path  = imageStorage.save(file, entity.getDriverId(), order);
                    return TaxiDriverImage.builder()
                            .taxiDriver(entity)
                            .imagePath(path)
                            .originalFilename(file.getOriginalFilename())
                            .displayOrder(order)
                            .build();
                })
                .collect(Collectors.toList());

        imageRepo.saveAll(imageEntities);
    }

    private TaxiDriverResponse toResponse(TaxiDriver e) {
        List<VehicleImageResponse> images = imageRepo
                .findByTaxiDriverIdOrderByDisplayOrderAsc(e.getId())
                .stream()
                .map(img -> VehicleImageResponse.builder()
                        .id(img.getId())
                        .imagePath(img.getImagePath())
                        .originalFilename(img.getOriginalFilename())
                        .displayOrder(img.getDisplayOrder())
                        .uploadedAt(img.getUploadedAt())
                        .build())
                .collect(Collectors.toList());

        return TaxiDriverResponse.builder()
                .id(e.getId())
                .driverId(e.getDriverId())
                .vehicleMake(e.getVehicleMake())
                .vehicleModel(e.getVehicleModel())
                .vehicleColor(e.getVehicleColor())
                .totalSeats(e.getTotalSeats())
                .licenseNumber(e.getLicenseNumber())
                .registrationNumber(e.getRegistrationNumber())
                .driverName(e.getDriverName())
                .phoneNumber(e.getPhoneNumber())
                .isOnline(e.getIsOnline())
                .images(images)
                .createdAt(e.getCreatedAt())
                .build();
    }
}
