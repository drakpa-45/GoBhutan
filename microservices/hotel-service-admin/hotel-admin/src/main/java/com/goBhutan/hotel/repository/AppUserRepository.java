
package com.goBhutan.hotel.repository;

import com.goBhutan.hotel.entity.AppUser;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByKeycloakId(String keycloakId);
}
