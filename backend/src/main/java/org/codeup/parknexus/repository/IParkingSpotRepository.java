package org.codeup.parknexus.repository;

import org.codeup.parknexus.domain.ParkingSpot;
import org.codeup.parknexus.domain.enums.SpotStatus;
import org.codeup.parknexus.domain.enums.SpotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IParkingSpotRepository extends JpaRepository<ParkingSpot, UUID> {

    // 1. USER DASHBOARD: Find available spots by type
    List<ParkingSpot> findByStatusAndType(SpotStatus status, SpotType type);

    // 2. ADMIN DASHBOARD: Statistics (Count is much faster than fetching the list)
    long countByStatus(SpotStatus status);

    // 3. NAVIGATION: View spots on a specific floor
    List<ParkingSpot> findByFloorId(UUID floorId);

    // 4. ADVANCED SEARCH (JPQL): Find available spots in a whole BUILDING
    // This automatically JOINs Spot -> Floor -> Building
    @Query("SELECT s FROM ParkingSpot s WHERE s.floor.building.id = :buildingId AND s.status = 'AVAILABLE'")
    List<ParkingSpot> findAvailableByBuilding(@Param("buildingId") UUID buildingId);

    List<ParkingSpot> findByStatus(SpotStatus status);
}

