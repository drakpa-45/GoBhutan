package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.DashboardStatsDTO;
import com.goBhutan.adminPanel.theater.entity.Movie;
import com.goBhutan.adminPanel.theater.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {

    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;
    private final TheaterBookingRepository bookingRepository;
    private final SeatRepository seatRepository;

    public DashboardService(MovieRepository movieRepository,
                            ScreeningRepository screeningRepository,
                            TheaterBookingRepository bookingRepository,
                            SeatRepository seatRepository) {
        this.movieRepository = movieRepository;
        this.screeningRepository = screeningRepository;
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
    }

    public DashboardStatsDTO getDashboardStats() {
        Long totalMovies = movieRepository.countActiveMovies();
        Long upcomingMovies = movieRepository.countByStatusAndIsActive(Movie.MovieStatus.UPCOMING);
        Long currentlyRunningMovies = movieRepository.countByStatusAndIsActive(Movie.MovieStatus.CURRENTLY_RUNNING);

        Double seatOccupancyRate = screeningRepository.calculateAverageOccupancyRate();
        if (seatOccupancyRate == null) {
            seatOccupancyRate = 0.0;
        }

        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        BigDecimal todayRevenue = bookingRepository.calculateTodayRevenue();
        if (todayRevenue == null) {
            todayRevenue = BigDecimal.ZERO;
        }

        Long totalBookings = bookingRepository.countConfirmedBookings();
        Long todayBookings = bookingRepository.countTodayBookings();

        return new DashboardStatsDTO(
                totalMovies,
                upcomingMovies,
                currentlyRunningMovies,
                seatOccupancyRate,
                totalRevenue,
                todayRevenue,
                totalBookings,
                todayBookings
        );
    }
}
