package org.codeup.statiocore.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationResponse {
    private UUID id;
    private UUID userId;
    private UUID spotId;
    private String spotNumber;
    private String buildingName;
    private Integer floorNumber;
    private String vehicleNumber;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String status;
    private OffsetDateTime createdAt;
}
