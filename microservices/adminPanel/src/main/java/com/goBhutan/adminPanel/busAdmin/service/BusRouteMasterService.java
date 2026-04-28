package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.BusRouteMasterRequest;
import com.goBhutan.adminPanel.busAdmin.dto.BusRouteMasterResponse;
import com.goBhutan.adminPanel.busAdmin.entity.BusRouteMaster;
import com.goBhutan.adminPanel.busAdmin.repository.BusRouteMasterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BusRouteMasterService {

    private final BusRouteMasterRepository routeMasterRepository;

    public BusRouteMasterResponse create(BusRouteMasterRequest request, String adminUserId) {
        validateDuplicate(null, request, adminUserId);

        BusRouteMaster routeMaster = new BusRouteMaster();
        routeMaster.setRouteName(normalize(request.getRouteName()));
        routeMaster.setActive(true);
        routeMaster.setAdminUserId(adminUserId);

        return toResponse(routeMasterRepository.save(routeMaster));
    }

    public BusRouteMasterResponse update(Long id, BusRouteMasterRequest request, String adminUserId) {
        BusRouteMaster routeMaster = routeMasterRepository.findByIdAndAdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route master not found"));

        validateDuplicate(id, request, adminUserId);

        routeMaster.setRouteName(normalize(request.getRouteName()));

        return toResponse(routeMasterRepository.save(routeMaster));
    }

    public BusRouteMasterResponse getById(Long id, String adminUserId) {
        return toResponse(routeMasterRepository.findByIdAndAdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route master not found")));
    }

    public List<BusRouteMasterResponse> getAll(String adminUserId, Boolean activeOnly) {
        List<BusRouteMaster> routeMasters = Boolean.TRUE.equals(activeOnly)
                ? routeMasterRepository.findByAdminUserIdAndActiveTrueOrderByRouteNameAsc(adminUserId)
                : routeMasterRepository.findByAdminUserIdOrderByRouteNameAsc(adminUserId);

        return routeMasters.stream()
                .map(this::toResponse)
                .toList();
    }

    public BusRouteMasterResponse disable(Long id, String adminUserId) {
        BusRouteMaster routeMaster = routeMasterRepository.findByIdAndAdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route master not found"));

        routeMaster.setActive(false);
        return toResponse(routeMasterRepository.save(routeMaster));
    }

    private void validateDuplicate(Long id, BusRouteMasterRequest request, String adminUserId) {
        String routeName = normalize(request.getRouteName());

        boolean exists = id == null
                ? routeMasterRepository.existsByAdminUserIdAndRouteNameIgnoreCase(
                adminUserId, routeName)
                : routeMasterRepository.existsByAdminUserIdAndRouteNameIgnoreCaseAndIdNot(
                adminUserId, routeName, id);

        if (exists) {
            throw new RuntimeException("Route master already exists for this admin");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private BusRouteMasterResponse toResponse(BusRouteMaster routeMaster) {
        return new BusRouteMasterResponse(
                routeMaster.getId(),
                routeMaster.getRouteName(),
                routeMaster.getActive(),
                routeMaster.getCreatedAt(),
                routeMaster.getUpdatedAt()
        );
    }
}
