package org.codeup.parknexus.web.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentRequest {
    @NotNull
    private UUID sessionId;

    @NotNull
    private BigDecimal amount;

    private String paymentMethod; // e.g., card, cash
}
