package org.codeup.parknexus.web.dto.user;

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
    private OffsetDateTime checkInTime;
    private String message;
}

