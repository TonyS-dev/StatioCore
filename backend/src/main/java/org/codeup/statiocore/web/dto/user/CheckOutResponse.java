package org.codeup.statiocore.web.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckOutResponse {
    private UUID sessionId;
    private UUID spotId;
    private String spotNumber;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime checkInTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime checkOutTime;

    private Long durationMinutes;
    private BigDecimal amountDue;

    // Payment fields
    private UUID paymentId;
    private String paymentStatus;      // "SUCCESS", "FAILED", "PENDING"
    private String transactionId;      // Transaction reference
    private String paymentMethod;      // Payment method used

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime paidAt;

    private String message;            // Status message
}
