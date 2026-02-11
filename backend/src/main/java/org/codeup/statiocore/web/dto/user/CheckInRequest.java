package org.codeup.statiocore.web.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckInRequest {
    @NotNull(message = "Spot ID is required")
    private UUID spotId;

    // optional reservation id if checking in for a reservation
    private UUID reservationId;

    private String vehicleNumber;
}
