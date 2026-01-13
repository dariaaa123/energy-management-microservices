package com.energy.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncMessage {
    
    @JsonProperty("type")
    private String type; // "USER" or "DEVICE"
    
    @JsonProperty("action")
    private String action; // "CREATE", "UPDATE", "DELETE"
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("deviceName")
    private String deviceName;
    
    @JsonProperty("maximumConsumptionValue")
    private Double maximumConsumptionValue;
    
    @JsonProperty("assignedUserId")
    private String assignedUserId;
}
