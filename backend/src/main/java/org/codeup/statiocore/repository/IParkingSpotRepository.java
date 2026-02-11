package org.codeup.statiocore.repository;

import org.codeup.statiocore.domain.ParkingSpot;
import org.codeup.statiocore.domain.enums.SpotStatus;
import org.codeup.statiocore.domain.enums.SpotType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IParkingSpotRepository extends JpaRepository<ParkingSpot, UUID>, JpaSpecificationExecutor<ParkingSpot> {

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

    /**
     * STRICT AVAILABILITY: Returns only truly available spots.
     * A spot is available ONLY if: status=AVAILABLE AND reservedBy IS NULL
     * Reserved spots are excluded even if status is AVAILABLE.
     */
    List<ParkingSpot> findByStatusAndReservedByIsNull(SpotStatus status);
    long countByStatusAndReservedByIsNull(SpotStatus status);

    /**
     * Eager fetch optimization: Prevents N+1 query problem by loading
     * Floor and Building relationships in a single query.
     * Use for admin dashboard where we need building/floor names.
     */
    @Query("SELECT s FROM ParkingSpot s LEFT JOIN FETCH s.floor f LEFT JOIN FETCH f.building LEFT JOIN FETCH s.reservedBy")
    List<ParkingSpot> findAllWithFloorAndBuilding();

    /**
     * Paginated version of findAllWithFloorAndBuilding.
     * Returns paginated spots with eager loaded floor and building relationships.
     */
    @Query("SELECT s FROM ParkingSpot s LEFT JOIN FETCH s.floor f LEFT JOIN FETCH f.building LEFT JOIN FETCH s.reservedBy")
    Page<ParkingSpot> findAllWithFloorAndBuilding(Pageable pageable);

    // Count methods for populating response DTOs
    long countByFloorId(UUID floorId);
    long countByFloorIdAndStatus(UUID floorId, SpotStatus status);
    long countByFloorBuildingId(UUID buildingId);
    long countByFloorBuildingIdAndStatus(UUID buildingId, SpotStatus status);
}
