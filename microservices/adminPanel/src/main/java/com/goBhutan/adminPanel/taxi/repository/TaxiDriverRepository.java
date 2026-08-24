package com.goBhutan.adminPanel.taxi.repository;

import com.goBhutan.adminPanel.taxi.entity.TaxiDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TaxiDriverRepository extends JpaRepository<TaxiDriver, Long> {

    Optional<TaxiDriver> findByDriverId(String driverId);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByRegistrationNumber(String registrationNumber);
}
