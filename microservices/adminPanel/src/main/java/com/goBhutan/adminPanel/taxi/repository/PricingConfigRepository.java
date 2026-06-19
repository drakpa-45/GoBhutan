package com.goBhutan.adminPanel.taxi.repository;

import com.goBhutan.adminPanel.taxi.entity.PricingConfig;
import com.goBhutan.adminPanel.taxi.enums.TripCategory;
import com.goBhutan.adminPanel.taxi.enums.TripMode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PricingConfigRepository extends JpaRepository<PricingConfig, Long> {
    Optional<PricingConfig> findByTripCategoryAndTripMode(TripCategory category, TripMode mode);
}
