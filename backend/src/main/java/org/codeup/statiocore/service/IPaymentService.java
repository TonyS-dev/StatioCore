package org.codeup.statiocore.service;

import org.codeup.statiocore.domain.enums.PaymentMethod;
import org.codeup.statiocore.web.dto.user.PaymentRequest;
import org.codeup.statiocore.web.dto.user.PaymentResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface IPaymentService {
    /**
     * Process payment for a parking session
     * @param sessionId The parking session ID
     * @param amount The amount to pay
     * @param method The payment method
     * @return Payment response with transaction details
     */
    PaymentResponse processPayment(UUID sessionId, BigDecimal amount, PaymentMethod method);

    /**
     * Calculate fee estimate for a session (before actual payment)
     * Used for preview before checkout
     * @param sessionId The parking session ID
     * @return Calculated amount
     */
    BigDecimal calculateFee(UUID sessionId);
}

