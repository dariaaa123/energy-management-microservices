package com.energy.monitoring.repository;

import com.energy.monitoring.entity.DeviceMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviceMeasurementRepository extends JpaRepository<DeviceMeasurement, Long> {
    
    List<DeviceMeasurement> findByDeviceIdAndTimestampBetween(
        String deviceId, LocalDateTime start, LocalDateTime end);
    
    @Modifying
    @Transactional
    void deleteByDeviceId(String deviceId);
    
    @Query("SELECT SUM(dm.measurementValue) FROM DeviceMeasurement dm " +
           "WHERE dm.deviceId = :deviceId AND dm.timestamp >= :start AND dm.timestamp < :end")
    Double sumMeasurementsByDeviceAndTimeRange(
        @Param("deviceId") String deviceId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(dm) FROM DeviceMeasurement dm " +
           "WHERE dm.deviceId = :deviceId AND dm.timestamp >= :start AND dm.timestamp < :end")
    Integer countMeasurementsByDeviceAndTimeRange(
        @Param("deviceId") String deviceId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
}
