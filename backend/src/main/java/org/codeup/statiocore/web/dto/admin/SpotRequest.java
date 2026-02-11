package org.codeup.statiocore.web.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SpotRequest {
    @NotNull(message = "Floor ID is required")
    private UUID floorId;

    @NotBlank(message = "Spot number is required")
    @Size(max = 20, message = "Spot number must not exceed 20 characters")
    private String spotNumber;

    @NotBlank(message = "Spot type is required")
    @Pattern(regexp = "STANDARD|VIP|HANDICAP|EV_CHARGING", message = "Invalid spot type")
    private String type;

    private UUID reservedByUserId;

    @NotBlank(message = "Spot status is required")
    @Pattern(regexp = "AVAILABLE|OCCUPIED|RESERVED|UNDER_MAINTENANCE", message = "Invalid spot status")
    private String status;
}

