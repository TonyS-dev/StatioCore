package org.codeup.parknexus.repository;

import org.codeup.parknexus.domain.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IFloorRepository extends JpaRepository<Floor, UUID> {
    // To get floors for a specific building (User Flow)
    List<Floor> findByBuildingIdOrderByFloorNumberAsc(UUID buildingId);

    // Validation: Prevent duplicate floor numbers in the same building
    boolean existsByBuildingIdAndFloorNumber(UUID buildingId, Integer floorNumber);

    // Count methods for populating response DTOs
    long countByBuildingId(UUID buildingId);
}
