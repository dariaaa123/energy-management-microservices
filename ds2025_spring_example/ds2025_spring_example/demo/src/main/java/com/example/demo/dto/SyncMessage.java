package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SyncMessage {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("action")
    private String action;
    
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
    
    public SyncMessage() {
    }
    
    public SyncMessage(String type, String action, String userId, String username, String name, 
                      String deviceId, String deviceName, Double maximumConsumptionValue, String assignedUserId) {
        this.type = type;
        this.action = action;
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.maximumConsumptionValue = maximumConsumptionValue;
        this.assignedUserId = assignedUserId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public Double getMaximumConsumptionValue() {
        return maximumConsumptionValue;
    }
    
    public void setMaximumConsumptionValue(Double maximumConsumptionValue) {
        this.maximumConsumptionValue = maximumConsumptionValue;
    }
    
    public String getAssignedUserId() {
        return assignedUserId;
    }
    
    public void setAssignedUserId(String assignedUserId) {
        this.assignedUserId = assignedUserId;
    }
    
    @Override
    public String toString() {
        return "SyncMessage{" +
                "type='" + type + '\'' +
                ", action='" + action + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", maximumConsumptionValue=" + maximumConsumptionValue +
                ", assignedUserId='" + assignedUserId + '\'' +
                '}';
    }
}
