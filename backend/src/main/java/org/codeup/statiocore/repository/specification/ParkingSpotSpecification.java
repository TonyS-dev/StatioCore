package org.codeup.statiocore.repository.specification;

import org.codeup.statiocore.domain.ParkingSpot;
import org.codeup.statiocore.domain.enums.SpotStatus;
import org.codeup.statiocore.domain.enums.SpotType;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParkingSpotSpecification {

    public static Specification<ParkingSpot> withFilters(UUID buildingId, UUID floorId, SpotType type, SpotStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by building if provided
            if (buildingId != null) {
                predicates.add(criteriaBuilder.equal(root.get("floor").get("building").get("id"), buildingId));
            }

            // Filter by floor if provided
            if (floorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("floor").get("id"), floorId));
            }

            // Filter by type if provided
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

