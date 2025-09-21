package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.RevenueReport;
import com.goBhutan.adminPanel.busAdmin.entity.Bookings;
import com.goBhutan.adminPanel.busAdmin.repository.BusBookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BusBookingService {
    @Autowired
    private BusBookingRepository busBookingRepository;

    public List<Bookings> getBookingsByOwner(String adminUserId) {
        return busBookingRepository.findBySchedule_Bus_AdminUserId(adminUserId);
    }

    public List<Bookings> getBookingsBySchedule(Long scheduleId) {
        return busBookingRepository.findBySchedule_Id(scheduleId);
    }

    public RevenueReport getRevenueReport(String adminUserId) {
        List<Bookings> bookings = busBookingRepository.findBySchedule_Bus_AdminUserId(adminUserId);
        BigDecimal totalRevenue =
                busBookingRepository.getTotalRevenueByAdminUserIdAndStatus(
                        adminUserId, Bookings.BookingStatus.CONFIRMED);

        List<RevenueReport.BookingInfo> bookingInfos = bookings.stream()
                .map(this::convertToBookingInfo)
                .collect(Collectors.toList());

        return new RevenueReport(totalRevenue, bookings.size(), bookingInfos);
    }

    public RevenueReport getRevenueReportByDateRange(
            String adminUserId, LocalDateTime startDate, LocalDateTime endDate) {

        List<Bookings> bookings =
                busBookingRepository.findBySchedule_Bus_AdminUserIdAndBookingTimeBetween(
                        adminUserId, startDate, endDate);

        BigDecimal totalRevenue =
                busBookingRepository.getRevenueByAdminUserIdAndDateRangeAndStatus(
                        adminUserId, startDate, endDate, Bookings.BookingStatus.CONFIRMED);

        List<RevenueReport.BookingInfo> bookingInfos = bookings.stream()
                .map(this::convertToBookingInfo)
                .collect(Collectors.toList());

        return new RevenueReport(totalRevenue, bookings.size(), bookingInfos);
    }



    private RevenueReport.BookingInfo convertToBookingInfo(Bookings booking) {
        RevenueReport.BookingInfo info = new RevenueReport.BookingInfo();
        info.setId(booking.getId());
        info.setPassengerName(booking.getPassengerName());
        info.setPassengerEmail(booking.getPassengerEmail());
        info.setSeatNumber(booking.getSeatNumber());
        info.setTotalAmount(booking.getTotalAmount());
        info.setBusNumber(booking.getSchedule().getBus().getBusNumber());
        info.setRoute(booking.getSchedule().getRoute().getSource() + " -> " +
                booking.getSchedule().getRoute().getDestination());
        info.setDepartureTime(booking.getSchedule().getDepartureTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        info.setStatus(booking.getStatus().toString());
        return info;
    }

    public List<Bookings> findByBusAdminUserId(String adminUserId) {
        return null;
    }
}
