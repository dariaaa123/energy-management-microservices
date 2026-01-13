package com.energy.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hourly_energy_consumption")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyEnergyConsumption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String deviceId;
    
    @Column(nullable = false)
    private LocalDateTime hourTimestamp;
    
    @Column(nullable = false)
    private Double totalConsumption;
    
    @Column(nullable = false)
    private Integer measurementCount;
    
    @Column(nullable = false)
    private LocalDateTime calculatedAt;
}
