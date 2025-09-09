package com.goBhutan.hotel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.goBhutan.hotel.entity.Room;
import com.goBhutan.hotel.entity.Room.RoomStatus;
import com.goBhutan.hotel.service.HotelService;
import com.goBhutan.hotel.service.RoomService;
import com.goBhutan.hotel.service.RoomTypeService;

import jakarta.validation.Valid;


@Controller
@RequestMapping("/rooms")
public class RoomController {
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private HotelService hotelService;
    
    @Autowired
    private RoomTypeService roomTypeService;
    
    @GetMapping
    public String listRooms(@RequestParam(required = false) Long hotelId, 
                           Model model, @AuthenticationPrincipal Jwt jwt) {
    	String adminUserId = jwt.getSubject(); // Keycloak user ID
        var hotels = hotelService.getHotelsByAdmin(adminUserId);
        model.addAttribute("hotels", hotels);
        
        if (hotelId != null) {
            var rooms = roomService.getRoomsByHotel(hotelId);
            model.addAttribute("rooms", rooms);
            model.addAttribute("selectedHotelId", hotelId);
            model.addAttribute("roomStatusCounts", roomService.getRoomStatusCounts(hotelId));
        }
        
        return "room/room-list";
    }
    
    @GetMapping("/new")
    public String newRoomForm(@RequestParam Long hotelId, Model model, @AuthenticationPrincipal Jwt jwt) {
    	String adminUserId = jwt.getSubject(); // Keycloak user ID
        var hotel = hotelService.getHotelById(hotelId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        
        Room room = new Room();
        room.setHotel(hotel);
        
        model.addAttribute("room", room);
        model.addAttribute("hotel", hotel);
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        model.addAttribute("roomStatuses", RoomStatus.values());
        return "room/room-form";
    }
    
    @GetMapping("/{id}/edit")
    public String editRoomForm(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomsByHotel(null).stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        model.addAttribute("room", room);
        model.addAttribute("hotel", room.getHotel());
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        model.addAttribute("roomStatuses", RoomStatus.values());
        return "room/room-form";
    }
    
    @PostMapping
    public String saveRoom(@Valid @ModelAttribute Room room, 
                          BindingResult bindingResult, 
                          Model model, 
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
            model.addAttribute("roomStatuses", RoomStatus.values());
            return "room/room-form";
        }
        
        roomService.saveRoom(room);
        redirectAttributes.addFlashAttribute("successMessage", 
            room.getId() == null ? "Room created successfully!" : "Room updated successfully!");
        return "redirect:/rooms?hotelId=" + room.getHotel().getId();
    }
    
    @PostMapping("/{id}/status")
    @ResponseBody
    public String updateRoomStatus(@PathVariable Long id, @RequestParam RoomStatus status) {
        roomService.updateRoomStatus(id, status);
        return "success";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Room room = roomService.getRoomsByHotel(null).stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (room != null) {
            Long hotelId = room.getHotel().getId();
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully!");
            return "redirect:/rooms?hotelId=" + hotelId;
        }
        
        return "redirect:/rooms";
    }
    
    @GetMapping("/calendar")
    public String roomCalendar(@RequestParam Long hotelId, Model model, @AuthenticationPrincipal Jwt jwt) {
    	String adminUserId = jwt.getSubject(); // Keycloak user ID
        var hotel = hotelService.getHotelById(hotelId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        var rooms = roomService.getRoomsByHotel(hotelId);
        
        model.addAttribute("hotel", hotel);
        model.addAttribute("rooms", rooms);
        return "room/room-calendar";
    }
}