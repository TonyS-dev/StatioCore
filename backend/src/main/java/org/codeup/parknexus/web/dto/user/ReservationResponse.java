package org.codeup.parknexus.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationResponse {
    private UUID id;
    private UUID spotId;
    private String spotIdentifier;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String status;
}
