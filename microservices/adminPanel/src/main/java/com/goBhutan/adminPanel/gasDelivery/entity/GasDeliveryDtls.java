package com.goBhutan.adminPanel.gasDelivery.entity;

import com.goBhutan.adminPanel.gasDelivery.enums.GasDeliveryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_gs_delivery_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GasDeliveryDtls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mobile Number is required")
    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;


    @Column(name = "cid_number")
    private String cidNumber;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "expected_delivery_time")
    private String expectedDeliveryTime;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GasDeliveryStatus status = GasDeliveryStatus.PENDING;

    @Column(name = "customer_remarks")
    private String customerRemarks;

    @Column(name = "admin_remarks")
    private String adminRemarks;

    @OneToMany(mappedBy = "gasDelivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GasDeliveryItemDtls> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "admin_user_id")
    private String adminUserId;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    public void addItem(GasDeliveryItemDtls item) {
        items.add(item);
        item.setGasDelivery(this);
    }
}
