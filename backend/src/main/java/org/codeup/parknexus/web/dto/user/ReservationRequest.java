package org.codeup.parknexus.web.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationRequest {
    @NotNull
    private UUID spotId;

    @NotNull
    private OffsetDateTime startTime;

    // optional duration in minutes
    private Integer durationMinutes;
}
