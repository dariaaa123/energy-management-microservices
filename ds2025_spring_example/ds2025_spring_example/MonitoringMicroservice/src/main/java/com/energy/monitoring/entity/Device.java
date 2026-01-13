package com.energy.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    
    @Id
    private String deviceId;
    
    @Column(nullable = false)
    private String name;
    
    private Double maximumConsumptionValue;
    
    private String userId;
}
