package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScreeningSearchRepository extends JpaRepository<Screening, Long> {

    // Search by movie name only (from today or given date)
    @Query("""
        SELECT s FROM Screening s
        JOIN FETCH s.hall h
        JOIN FETCH h.theater t
        JOIN FETCH t.location l
        WHERE LOWER(s.movieName) LIKE LOWER(CONCAT('%', :movieName, '%'))
          AND s.screeningDate >= :fromDate
          AND s.isActive = true
          AND h.isActive = true
          AND t.isActive = true
        ORDER BY t.name ASC, h.name ASC, s.screeningDate ASC, s.startTime ASC
        """)
    List<Screening> findActiveScreeningsByMovieName(
            @Param("movieName") String movieName,
            @Param("fromDate") LocalDate fromDate
    );

    // Search by movie name + dzongkhag (district filter from mobile)
    @Query("""
        SELECT s FROM Screening s
        JOIN FETCH s.hall h
        JOIN FETCH h.theater t
        JOIN FETCH t.location l
        WHERE LOWER(s.movieName) LIKE LOWER(CONCAT('%', :movieName, '%'))
          AND s.screeningDate >= :fromDate
          AND s.isActive = true
          AND h.isActive = true
          AND t.isActive = true
          AND LOWER(l.dzongkhag) LIKE LOWER(CONCAT('%', :dzongkhag, '%'))
        ORDER BY t.name ASC, h.name ASC, s.screeningDate ASC, s.startTime ASC
        """)
    List<Screening> findActiveScreeningsByMovieNameAndDzongkhag(
            @Param("movieName") String movieName,
            @Param("fromDate") LocalDate fromDate,
            @Param("dzongkhag") String dzongkhag
    );
}