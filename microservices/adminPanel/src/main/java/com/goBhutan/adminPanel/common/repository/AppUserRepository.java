package com.goBhutan.adminPanel.common.repository;

import com.goBhutan.adminPanel.common.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByKeycloakId(String keycloakId);
    Optional<AppUser> findByUsername(String username); // 🔹 new

    // Find all staff for a specific hotel/bus/theater
    List<AppUser> findByEntityId(String entityId);

    // Find all staff for a specific entity type
    List<AppUser> findByEntityType(String entityType);

    // Find staff for specific entity and type
    List<AppUser> findByEntityIdAndEntityType(String entityId, String entityType);
}
