package org.codeup.statiocore.web.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FloorRequest {
    @NotNull(message = "Building ID is required")
    private UUID buildingId;

    @NotNull(message = "Floor number is required")
    private Integer floorNumber;
}

