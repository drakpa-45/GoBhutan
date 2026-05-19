package com.goBhutan.adminPanel.gasDelivery.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_gs_delivery_item_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GasDeliveryItemDtls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gas_delivery_id", nullable = false)
    private GasDeliveryDtls gasDelivery;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gas_config_id", nullable = false)
    private GasConfigMaster gasConfig;

    @NotNull(message = "Gas type is required")
    @Column(name = "gas_type", nullable = false)
    private String gasType;

    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be zero or greater")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
