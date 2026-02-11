package org.codeup.statiocore.web.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSessionResponse {
    private UUID id;
    private UUID userId;
    private UUID spotId;
    private String spotNumber;
    private String buildingName;
    private Integer floorNumber;
    private String vehicleNumber;
    private ParkingSpotResponse spot;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime checkInTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime checkOutTime;
    
    private Long duration; // in minutes
    private BigDecimal fee;
    private String transactionId;
    private String paymentMethod;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime updatedAt;
}

