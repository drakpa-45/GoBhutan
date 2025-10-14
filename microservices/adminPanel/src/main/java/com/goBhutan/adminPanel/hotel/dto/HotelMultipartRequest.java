package com.goBhutan.adminPanel.hotel.dto;

import com.goBhutan.adminPanel.hotel.entity.Hotel;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "Request to create or update a hotel with images")
public class HotelMultipartRequest {

    @Schema(description = "Hotel details in JSON", required = true, implementation = Hotel.class)
    private Hotel hotel;

    @Schema(description = "Hotel-level images", type = "array", format = "binary")
    private List<MultipartFile> hotelImages;

    @Schema(description = "Room images per room")
    private List<RoomImageUploadDTO> roomImages;

    // Getters and setters
    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public List<MultipartFile> getHotelImages() { return hotelImages; }
    public void setHotelImages(List<MultipartFile> hotelImages) { this.hotelImages = hotelImages; }

    public List<RoomImageUploadDTO> getRoomImages() { return roomImages; }
    public void setRoomImages(List<RoomImageUploadDTO> roomImages) { this.roomImages = roomImages; }
}
