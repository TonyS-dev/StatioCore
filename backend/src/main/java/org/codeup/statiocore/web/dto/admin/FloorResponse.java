package org.codeup.statiocore.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorResponse {
    private UUID id;
    private UUID buildingId;
    private String buildingName;
    private Integer floorNumber;
    private Integer spotCount;
    private Integer totalSpots;
    private Integer availableSpots;
    private OffsetDateTime createdAt;
}

