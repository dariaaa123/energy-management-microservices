package com.energy.monitoring.repository;

import com.energy.monitoring.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    // MonitoringService doesn't need to query devices by user
    // It only tracks device measurements regardless of ownership
}
