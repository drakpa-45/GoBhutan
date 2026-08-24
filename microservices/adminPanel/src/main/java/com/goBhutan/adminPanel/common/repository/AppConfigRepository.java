package com.goBhutan.adminPanel.common.repository;

import com.goBhutan.adminPanel.common.entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {

    List<AppConfig> findByActiveTrueOrderByConfigForAscConfigIdAsc();

    List<AppConfig> findByConfigForAndActiveTrueOrderByConfigIdAsc(String configFor);
}
