package com.goBhutan.adminPanel.taxi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class TaxiImageStorageService {

    @Value("${file.upload.directory:/opt/uploads/taxi}")
    private String uploadDirectory;

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED = Set.of(
            "image/jpeg", "image/png", "image/webp");

    public String save(MultipartFile file, String driverId, int index) {
        validate(file);
        try {
            Path dir = Paths.get(uploadDirectory);
            Files.createDirectories(dir);

            String ext      = getExtension(file.getOriginalFilename());
            String filename = driverId + "_" + index + "_" + UUID.randomUUID() + "." + ext;
            Path   target   = dir.resolve(filename);
            file.transferTo(target);

            String relativePath = "/uploads/taxi/" + filename;
            log.info("Taxi image saved [driver={}, index={}]: {}", driverId, index, relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to save taxi image [driver={}, index={}]: {}", driverId, index, e.getMessage());
            throw new RuntimeException("Image upload failed. Please try again.");
        }
    }

    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            String filename = Paths.get(relativePath).getFileName().toString();
            Path   target   = Paths.get(uploadDirectory).resolve(filename);
            Files.deleteIfExists(target);
            log.info("Deleted taxi image: {}", target);
        } catch (IOException e) {
            log.warn("Could not delete taxi image {}: {}", relativePath, e.getMessage());
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File must not be empty.");
        if (file.getSize() > MAX_SIZE_BYTES)
            throw new IllegalArgumentException(
                    "'" + file.getOriginalFilename() + "' exceeds 5 MB limit.");
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType))
            throw new IllegalArgumentException(
                    "Only JPG, PNG, or WEBP images are allowed. Got: " + contentType);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}