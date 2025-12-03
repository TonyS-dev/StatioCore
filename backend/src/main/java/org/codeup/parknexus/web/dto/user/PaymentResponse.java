package org.codeup.parknexus.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
    private UUID sessionId;
    private BigDecimal amount;
    private String status; // e.g., "SUCCESS"
    private String transactionId;
}
