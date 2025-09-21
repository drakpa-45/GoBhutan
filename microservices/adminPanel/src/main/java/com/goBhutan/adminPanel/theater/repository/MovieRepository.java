//package com.goBhutan.adminPanel.theater.repository;
//
//import com.goBhutan.adminPanel.theater.entity.*;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//@Repository
//public interface MovieRepository extends JpaRepository<Movie, String> {
//    Page<Movie> findAllByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
//    Page<Movie> findByStatusAndIsActiveTrueOrderByCreatedAtDesc(Movie.MovieStatus status, Pageable pageable);
//    Page<Movie> findByStudioIdAndIsActiveTrueOrderByCreatedAtDesc(String studioId, Pageable pageable);
//
//    @Query("SELECT m FROM Movie m JOIN m.categories c WHERE c.id = :categoryId AND m.isActive = true ORDER BY m.createdAt DESC")
//    Page<Movie> findByCategoryIdAndIsActiveTrue(@Param("categoryId") String categoryId, Pageable pageable);
//
//    @Query("SELECT m FROM Movie m WHERE m.title ILIKE %:title% AND m.isActive = true ORDER BY m.createdAt DESC")
//    Page<Movie> findByTitleContainingIgnoreCaseAndIsActiveTrue(@Param("title") String title, Pageable pageable);
//
//    @Query("SELECT COUNT(m) FROM Movie m WHERE m.isActive = true")
//    Long countActiveMovies();
//
//    @Query("SELECT COUNT(m) FROM Movie m WHERE m.status = :status AND m.isActive = true")
//    Long countByStatusAndIsActive(@Param("status") Movie.MovieStatus status);
//
//    List<Movie> findByStatusAndIsActiveTrue(Movie.MovieStatus status);
//}
