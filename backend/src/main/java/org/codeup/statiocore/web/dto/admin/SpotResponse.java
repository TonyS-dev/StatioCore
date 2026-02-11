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
public class SpotResponse {
    private UUID id;
    private UUID floorId;
    private Integer floorNumber;
    private String buildingName;
    private String spotNumber;
    private String type;
    private String status;
    private UUID reservedByUserId;
    private String reservedByUserName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

