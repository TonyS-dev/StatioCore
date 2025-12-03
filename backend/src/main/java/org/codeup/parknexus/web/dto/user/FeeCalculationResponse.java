package org.codeup.parknexus.web.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FeeCalculationResponse {
    private UUID sessionId;
    private String spotNumber;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime checkInTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime calculatedCheckOutTime;

    private Long durationMinutes;
    private BigDecimal hourlyRate;
    private BigDecimal amountDue;
    private String spotType;
    private String message;  // e.g., "2h 15min @ $10/hr = $22.50"
}

