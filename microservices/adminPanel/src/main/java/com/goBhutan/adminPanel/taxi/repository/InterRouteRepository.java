package com.goBhutan.adminPanel.taxi.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.goBhutan.adminPanel.taxi.entity.InterRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InterRouteRepository extends JpaRepository<InterRoute, Long> {

    List<InterRoute> findByOriginDzongkhagAndDestinationDzongkhagAndIsActiveTrue(
            String origin, String destination);

    Optional<InterRoute> findByIdAndIsActiveTrue(Long id);

    /** Atomically decrement available seats — prevents overbooking */
    @Modifying
    @Query("UPDATE InterRoute r SET r.availableSeats = r.availableSeats - :seats " +
           "WHERE r.id = :id AND r.availableSeats >= :seats")
    int decrementSeats(@Param("id") Long id, @Param("seats") int seats);

    /** Restore seats on cancellation */
    @Modifying
    @Query("UPDATE InterRoute r SET r.availableSeats = r.availableSeats + :seats WHERE r.id = :id")
    int incrementSeats(@Param("id") Long id, @Param("seats") int seats);

    List<InterRoute> findByDriverIdAndIsActiveTrue(String driverId);
}
