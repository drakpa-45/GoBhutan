package com.goBhutan.adminPanel.gasDelivery.service;

import com.goBhutan.adminPanel.gasDelivery.dto.GasConfigMasterRequest;
import com.goBhutan.adminPanel.gasDelivery.dto.GasConfigMasterResponse;
import com.goBhutan.adminPanel.gasDelivery.entity.GasConfigMaster;
import com.goBhutan.adminPanel.gasDelivery.repository.GasConfigMasterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GasConfigMasterService {

    private final GasConfigMasterRepository gasConfigMasterRepository;

    public GasConfigMasterResponse create(GasConfigMasterRequest request, String adminUserId) {
        validateDuplicate(null, request, adminUserId);

        GasConfigMaster gasConfigMaster = new GasConfigMaster();
        gasConfigMaster.setGasType(normalize(request.getGasType()));
        gasConfigMaster.setQuantity(request.getQuantity());
        gasConfigMaster.setActive(true);
        gasConfigMaster.setAdminUserId(adminUserId);

        return toResponse(gasConfigMasterRepository.save(gasConfigMaster));
    }

    public GasConfigMasterResponse update(Long id, GasConfigMasterRequest request, String adminUserId) {
        GasConfigMaster gasConfigMaster = gasConfigMasterRepository.findByIdAndAdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route master not found"));

        validateDuplicate(id, request, adminUserId);

        gasConfigMaster.setGasType(normalize(request.getGasType()));
        gasConfigMaster.setQuantity(request.getQuantity());

        return toResponse(gasConfigMasterRepository.save(gasConfigMaster));
    }

    public GasConfigMasterResponse getById(Long id) {
        return toResponse(gasConfigMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gas Config master not found")));
    }

    public List<GasConfigMasterResponse> getAll(Boolean activeOnly) {
        List<GasConfigMaster> gasConfigMasters = Boolean.FALSE.equals(activeOnly)
                ? gasConfigMasterRepository.findAllByOrderByGasTypeAsc()
                : gasConfigMasterRepository.findByActiveTrueOrderByGasTypeAsc();

        return gasConfigMasters.stream()
                .map(this::toResponse)
                .toList();
    }

    public GasConfigMasterResponse disable(Long id, String adminUserId) {
        GasConfigMaster gasConfigMaster = gasConfigMasterRepository.findByIdAndAdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Gas Config master not found"));

        gasConfigMaster.setActive(false);
        return toResponse(gasConfigMasterRepository.save(gasConfigMaster));
    }

    private void validateDuplicate(Long id, GasConfigMasterRequest request, String adminUserId) {
        String gasType = normalize(request.getGasType());

        boolean exists = id == null
                ? gasConfigMasterRepository.existsByAdminUserIdAndGasTypeIgnoreCase(
                adminUserId, gasType)
                : gasConfigMasterRepository.existsByAdminUserIdAndGasTypeIgnoreCaseAndIdNot(
                adminUserId, gasType, id);

        if (exists) {
            throw new RuntimeException("Gas Type config master already exists.");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private GasConfigMasterResponse toResponse(GasConfigMaster gasConfigMaster) {
        return new GasConfigMasterResponse(
                gasConfigMaster.getId(),
                gasConfigMaster.getGasType(),
                gasConfigMaster.getQuantity(),
                gasConfigMaster.getActive(),
                gasConfigMaster.getCreatedAt(),
                gasConfigMaster.getUpdatedAt()
        );
    }
}
