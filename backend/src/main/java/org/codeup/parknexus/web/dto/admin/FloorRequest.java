package org.codeup.parknexus.web.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FloorRequest {
    @NotNull(message = "Building ID is required")
    private UUID buildingId;

    @NotBlank(message = "Floor name is required")
    @Size(max = 50, message = "Floor name must not exceed 50 characters")
    private String name;
}

