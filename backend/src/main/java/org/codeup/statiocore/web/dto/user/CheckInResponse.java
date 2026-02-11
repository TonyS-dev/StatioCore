package org.codeup.statiocore.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckInResponse {
    private UUID sessionId;
    private UUID spotId;
    private String spotNumber;
    private String vehicleNumber;
    private String buildingName;
    private String floorName;
    private OffsetDateTime checkInTime;
    private String status;
    private String message;
}

