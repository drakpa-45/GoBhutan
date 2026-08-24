package com.goBhutan.adminPanel.hotel.service;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class StorageService {

    private final Path rootLocation = Paths.get("D:/GoBhutan/uploads");

    public StorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage folder", e);
        }
    }

    /**
     * Uploads a file and returns its URL (path)
     */
    public String upload(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot store empty file.");
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Save file to disk
            Path destinationFile = rootLocation.resolve(filename).normalize().toAbsolutePath();
            file.transferTo(destinationFile.toFile());

            // Return relative URL or absolute path
            return destinationFile.toString(); // Or "/uploads/" + filename if serving via HTTP

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }
}

