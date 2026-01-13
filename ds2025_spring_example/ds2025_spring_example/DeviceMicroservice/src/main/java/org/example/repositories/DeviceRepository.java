package org.example.repositories;

import org.example.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    /**
     * Example: JPA generate query by existing field
     */
    List<Device> findByName(String name);

    /**
     * Example: Custom query
     */
    @Query(value = "SELECT p " +
            "FROM Device p " +
            "WHERE p.name = :name " +
            "AND p.maximumConsumptionValue >= 60  ")
    Optional<Device> findSeniorsByName(@Param("name") String name);

    /**
     * Find all devices for a specific user
     */
    List<Device> findByUserId(String userId);

}
