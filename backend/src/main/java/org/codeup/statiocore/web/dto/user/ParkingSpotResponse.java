package org.codeup.statiocore.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpotResponse {
    private UUID id;
    private String spotNumber;
    private String type;
    private String floorName;
    private Integer floorNumber;
    private String buildingName;
    private String buildingAddress;
    private String status;
    private Double hourlyRate;
}

