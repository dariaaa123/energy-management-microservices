package org.example.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;


import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "Devices")
public class Device implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "maximum_consumption_value", nullable = false)
    private int maximumConsumptionValue;

    @Column(name = "user_id")
    private String userId;

    public Device(String name, int maximumConsumptionValue, String userId) {
        this.name = name;
        this.maximumConsumptionValue = maximumConsumptionValue;
        this.userId = userId;
    }

    public Device(String name, int maximumConsumptionValue) {
        this.name = name;
        this.maximumConsumptionValue = maximumConsumptionValue;
    }

    public Device() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaximumConsumptionValue() {
        return maximumConsumptionValue;
    }

    public void setMaximumConsumptionValue(int maximumConsumptionValue) {
        this.maximumConsumptionValue = maximumConsumptionValue;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
