package org.codeup.parknexus.web.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SpotUpdateRequest {
    @NotNull(message = "Spot ID is required")
    private UUID spotId;

    @NotNull(message = "Status is required")
    @Pattern(regexp = "AVAILABLE|OCCUPIED|RESERVED|MAINTENANCE", message = "Invalid status")
    private String status;
}

