package com.energy.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String type;
    private String userId;
    private String deviceId;
    private String deviceName;
    private Double currentConsumption;
    private Double maxAllowed;
    private String message;
    private String timestamp;
}
