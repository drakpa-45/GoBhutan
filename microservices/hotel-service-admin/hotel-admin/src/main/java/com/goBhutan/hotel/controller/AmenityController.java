package com.goBhutan.hotel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.goBhutan.hotel.entity.Amenity;
import com.goBhutan.hotel.entity.Amenity.AmenityCategory;
import com.goBhutan.hotel.service.AmenityService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/amenities")
public class AmenityController {
    
    @Autowired
    private AmenityService amenityService;
    
    @GetMapping
    public String listAmenities(Model model) {
        model.addAttribute("amenitiesByCategory", amenityService.getAmenitiesByCategory());
        return "amenity/amenity-list";
    }
    
    @GetMapping("/new")
    public String newAmenityForm(Model model) {
        model.addAttribute("amenity", new Amenity());
        model.addAttribute("categories", AmenityCategory.values());
        return "amenity/amenity-form";
    }
    
    @PostMapping
    public String saveAmenity(@Valid @ModelAttribute Amenity amenity, 
                             BindingResult bindingResult, 
                             Model model, 
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", AmenityCategory.values());
            return "amenity/amenity-form";
        }
        
        amenityService.saveAmenity(amenity);
        redirectAttributes.addFlashAttribute("successMessage", "Amenity saved successfully!");
        return "redirect:/amenities";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteAmenity(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        amenityService.deleteAmenity(id);
        redirectAttributes.addFlashAttribute("successMessage", "Amenity deleted successfully!");
        return "redirect:/amenities";
    }
}
