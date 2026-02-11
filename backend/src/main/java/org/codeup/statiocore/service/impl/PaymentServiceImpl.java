package org.codeup.statiocore.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeup.statiocore.domain.Payment;
import org.codeup.statiocore.domain.ParkingSession;
import org.codeup.statiocore.domain.enums.PaymentMethod;
import org.codeup.statiocore.domain.enums.PaymentStatus;
import org.codeup.statiocore.domain.enums.SessionStatus;
import org.codeup.statiocore.exception.PaymentException;
import org.codeup.statiocore.exception.ResourceNotFoundException;
import org.codeup.statiocore.repository.IPaymentRepository;
import org.codeup.statiocore.repository.IParkingSessionRepository;
import org.codeup.statiocore.service.IActivityLogService;
import org.codeup.statiocore.service.IPaymentService;
import org.codeup.statiocore.web.dto.user.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Payment processing service (simulated for MVP).
 *
 * Validates session completion, enforces minimum charge,
 * prevents duplicate payments, generates transaction IDs.
 *
 * @author TonyS-dev
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final IPaymentRepository paymentRepository;
    private final IParkingSessionRepository parkingSessionRepository;
    private final IActivityLogService activityLogService;

    // Pricing configuration (future: move to database/config)
    private static final BigDecimal HOURLY_RATE = new BigDecimal("5.00");
    private static final BigDecimal MINIMUM_FEE = new BigDecimal("2.00");

    @Override
    @Transactional
    public PaymentResponse processPayment(UUID sessionId, BigDecimal amount, PaymentMethod method) {
        log.info("Processing payment for session: {} with method: {}", sessionId, method);

        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking session not found"));

        // Validate: Session must be COMPLETED before accepting payment
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new PaymentException("Session must be completed before payment");
        }

        // Validate: Amount must be calculated and positive (minimum $1.00 enforced upstream)
        if (session.getAmountDue() == null) {
            throw new PaymentException("Amount due not calculated. Please complete the checkout process.");
        }
        if (session.getAmountDue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Invalid parking charge. Amount must be greater than $0.00");
        }

        // Prevent duplicate payment (idempotency check)
        if (paymentRepository.findAll().stream()
                .anyMatch(p -> p.getSession().getId().equals(sessionId)
                        && p.getStatus() == PaymentStatus.SUCCESS)) {
            throw new PaymentException("Session already paid");
        }

        // Create payment record
        Payment payment = Payment.builder()
                .session(session)
                .amount(amount)
                .method(method)
                .currency("USD")
                .status(PaymentStatus.SUCCESS) // Simulated success
                .transactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        // Log payment activity
        activityLogService.log(session.getUser(), "PAYMENT_PROCESSED", 
            String.format("Payment processed successfully. Amount: $%s, Method: %s, Transaction: %s", 
                payment.getAmount(), method.name(), payment.getTransactionReference()));

        log.info("Payment processed successfully: {} for session: {}", payment.getTransactionReference(), sessionId);

        return PaymentResponse.builder()
                .paymentId(payment.getId()) // Include the actual payment ID from DB
                .sessionId(sessionId)
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .transactionId(payment.getTransactionReference())
                .method(payment.getMethod().name())
                .timestamp(payment.getCreatedAt())
                .build();
    }

    @Override
    public BigDecimal calculateFee(UUID sessionId) {
        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking session not found"));

        if (session.getCheckInTime() == null) {
            throw new PaymentException("Session has no check-in time");
        }

        OffsetDateTime endTime = session.getCheckOutTime() != null
                ? session.getCheckOutTime()
                : OffsetDateTime.now();

        Duration duration = Duration.between(session.getCheckInTime(), endTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        // Calculate fee: charge per hour, round up partial hours
        BigDecimal calculatedFee = HOURLY_RATE.multiply(BigDecimal.valueOf(hours));
        if (minutes > 0) {
            calculatedFee = calculatedFee.add(HOURLY_RATE);
        }

        // Apply minimum fee
        return calculatedFee.max(MINIMUM_FEE);
    }
}

