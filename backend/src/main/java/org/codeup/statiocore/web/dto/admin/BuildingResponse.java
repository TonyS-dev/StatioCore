package org.codeup.statiocore.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BuildingResponse {
    private UUID id;
    private String name;
    private String address;
    private OffsetDateTime createdAt;
    private Integer totalFloors;
    private Integer totalSpots;
    private Integer occupiedSpots;
    private Integer availableSpots;
}

