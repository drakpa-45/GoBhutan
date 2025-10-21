package com.goBhutan.adminPanel.busAdmin.entity;

import com.goBhutan.adminPanel.busAdmin.dto.SeatType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tbl_bs_seat_config")
public class SeatConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_no", nullable = false)
    private Integer startNo;

    @Column(name = "end_no", nullable = false)
    private Integer endNo;

   // @Column(name = "seat_type", nullable = false)
    //private Integer seatType; // 1=Front, 2=Window, 3=Aisle, 4=Front-Window, 5=Back

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStartNo() {
        return startNo;
    }

    public void setStartNo(Integer startNo) {
        this.startNo = startNo;
    }

    public Integer getEndNo() {
        return endNo;
    }

    public void setEndNo(Integer endNo) {
        this.endNo = endNo;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }
}
