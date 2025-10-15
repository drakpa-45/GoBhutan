package com.goBhutan.adminPanel.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "DTO for uploading room images")
public class RoomImageUploadDTO {

    @Schema(description = "Room ID", example = "1")
    private Long roomId;

    @Schema(description = "Images for the room", type = "array", format = "binary")
    private List<MultipartFile> images;

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public List<MultipartFile> getImages() { return images; }
    public void setImages(List<MultipartFile> images) { this.images = images; }
}
