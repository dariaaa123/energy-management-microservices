package com.energy.monitoring.repository;

import com.energy.monitoring.entity.HourlyEnergyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HourlyEnergyConsumptionRepository extends JpaRepository<HourlyEnergyConsumption, Long> {
    
    Optional<HourlyEnergyConsumption> findByDeviceIdAndHourTimestamp(String deviceId, LocalDateTime hourTimestamp);
    
    List<HourlyEnergyConsumption> findByDeviceIdOrderByHourTimestampDesc(String deviceId);
    
    List<HourlyEnergyConsumption> findByDeviceIdAndHourTimestampBetween(
        String deviceId, LocalDateTime start, LocalDateTime end);
    
    @Modifying
    @Transactional
    void deleteByDeviceId(String deviceId);
}
