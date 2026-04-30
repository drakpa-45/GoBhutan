package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.BusRouteMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRouteMasterRepository extends JpaRepository<BusRouteMaster, Long> {

    List<BusRouteMaster> findAllByOrderByRouteNameAsc();

    List<BusRouteMaster> findByActiveTrueOrderByRouteNameAsc();

    Optional<BusRouteMaster> findByIdAndAdminUserId(Long id, String adminUserId);

    boolean existsByAdminUserIdAndRouteNameIgnoreCase(
            String adminUserId,
            String routeName
    );

    boolean existsByAdminUserIdAndRouteNameIgnoreCaseAndIdNot(
            String adminUserId,
            String routeName,
            Long id
    );
}
