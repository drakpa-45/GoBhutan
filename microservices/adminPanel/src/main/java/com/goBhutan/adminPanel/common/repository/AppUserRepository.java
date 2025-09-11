
package com.goBhutan.adminPanel.common.repository;

import com.goBhutan.adminPanel.common.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

;import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByKeycloakId(String keycloakId);
}
