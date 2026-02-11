package org.codeup.statiocore.web.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codeup.statiocore.web.validation.FutureOrPresent;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationRequest {
    @NotNull(message = "Spot ID is required")
    private UUID spotId;

    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Dates in the past are not available. Please select a future date and time.")
    private OffsetDateTime startTime;

    // optional vehicle number for the reservation
    private String vehicleNumber;

    // optional duration in minutes
    private Integer durationMinutes;
}
