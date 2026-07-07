package com.goBhutan.adminPanel.taxi.repository;

import com.goBhutan.adminPanel.taxi.entity.TaxiDriverImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaxiDriverImageRepository extends JpaRepository<TaxiDriverImage, Long> {

    List<TaxiDriverImage> findByTaxiDriverIdOrderByDisplayOrderAsc(Long taxiDriverId);

    Optional<TaxiDriverImage> findByIdAndTaxiDriverId(Long imageId, Long taxiDriverId);

    /** Shift display order when an image is deleted */
    @Modifying
    @Query("""
        UPDATE TaxiDriverImage i
        SET i.displayOrder = i.displayOrder - 1
        WHERE i.taxiDriver.id = :taxiDriverId
          AND i.displayOrder > :deletedOrder
        """)
    void shiftOrderAfterDelete(@Param("taxiDriverId") Long taxiDriverId,
                               @Param("deletedOrder") int deletedOrder);

    int countByTaxiDriverId(Long taxiDriverId);
}
