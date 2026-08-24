package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    List<Screening> findByHallIdOrderByScreeningDateAscStartTimeAsc(Long hallId);

    List<Screening> findByScreeningDate(LocalDate date);

    List<Screening> findByMovieNameContainingIgnoreCase(String movieName);
}
