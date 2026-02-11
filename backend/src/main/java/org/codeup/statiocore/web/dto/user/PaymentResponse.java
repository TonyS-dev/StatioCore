package org.codeup.statiocore.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
    private UUID paymentId; // ID of the payment record
    private UUID sessionId;
    private BigDecimal amount;
    private String status; // e.g., "SUCCESS"
    private String transactionId;
    private String method; // Payment method used
    private OffsetDateTime timestamp; // When payment was processed
}
