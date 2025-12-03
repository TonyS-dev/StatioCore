package org.codeup.parknexus.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.ParkingSession;
import org.codeup.parknexus.domain.ParkingSpot;
import org.codeup.parknexus.domain.User;
import org.codeup.parknexus.domain.enums.PaymentMethod;
import org.codeup.parknexus.domain.enums.SessionStatus;
import org.codeup.parknexus.domain.enums.SpotStatus;
import org.codeup.parknexus.domain.enums.SpotType;
import org.codeup.parknexus.exception.ConflictException;
import org.codeup.parknexus.exception.ResourceNotFoundException;
import org.codeup.parknexus.repository.IParkingSessionRepository;
import org.codeup.parknexus.repository.IParkingSpotRepository;
import org.codeup.parknexus.repository.IUserRepository;
import org.codeup.parknexus.repository.specification.ParkingSpotSpecification;
import org.codeup.parknexus.domain.enums.SessionStatus;

import org.codeup.parknexus.service.IActivityLogService;
import org.codeup.parknexus.service.IParkingService;
import org.codeup.parknexus.service.IPaymentService;
import org.codeup.parknexus.service.strategy.FeeCalculatorFactory;
import org.codeup.parknexus.service.strategy.IFeeCalculationStrategy;
import org.codeup.parknexus.web.dto.user.CheckOutResponse;
import org.codeup.parknexus.web.dto.user.FeeCalculationResponse;
import org.codeup.parknexus.web.dto.user.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Core parking operations service.
 *
 * Handles: check-in, checkout, fee calculation, spot availability.
 * Business rules:
 * - One active session per user
 * - Minimum fee $1.00
 * - Spot freed before payment processed
 *
 * @author TonyS-dev
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ParkingServiceImpl implements IParkingService {
    private static final Logger logger = LoggerFactory.getLogger(ParkingServiceImpl.class);

    private final IParkingSpotRepository spotRepository;
    private final IParkingSessionRepository sessionRepository;
    private final IUserRepository userRepository;
    private final IActivityLogService logService;
    private final IPaymentService paymentService;
    private final FeeCalculatorFactory feeCalculatorFactory;

    @Override
    @CacheEvict(value = "availableSpots", allEntries = true)
    public ParkingSession checkIn(UUID userId, UUID spotId, String vehicleNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Prevent multiple active sessions for the same user
        Optional<ParkingSession> existingSession = sessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE);
        if (existingSession.isPresent()) {
            throw new ConflictException("You already have an active parking session. Please check out first.");
        }
        
        ParkingSpot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));

        if (spot.getStatus() != SpotStatus.AVAILABLE) {
            throw new ConflictException("Spot not available");
        }

        spot.setStatus(SpotStatus.OCCUPIED);
        spotRepository.save(spot);

        ParkingSession session = ParkingSession.builder()
                .user(user)
                .spot(spot)
                .checkInTime(OffsetDateTime.now())
                .vehicleNumber(vehicleNumber)
                .status(SessionStatus.ACTIVE)
                .build();

        session = sessionRepository.save(session);
        logService.log(user, "CHECK_IN", "Spot: " + spot.getSpotNumber());

        return session;
    }

    @Override
    public FeeCalculationResponse calculateFee(UUID sessionId) {
        logger.info("Calculating fee for session: {}", sessionId);

        ParkingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found for ID: " + sessionId));

        if (session.getCheckOutTime() != null) {
            logger.warn("Attempted to calculate fee for already checked out session: {}", sessionId);
            throw new IllegalArgumentException("Session already checked out");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Duration duration = Duration.between(session.getCheckInTime(), now);

        // Get fee strategy based on spot type
        ParkingSpot spot = session.getSpot();
        String strategyKey = getStrategyKeyForSpotType(spot.getType());
        IFeeCalculationStrategy strategy = feeCalculatorFactory.getStrategy(strategyKey);
        BigDecimal fee = strategy.calculateFee(duration);

        // Hourly rate is $10 for standard spots, $15 for VIP
        BigDecimal hourlyRate = spot.getType() == SpotType.VIP ? BigDecimal.valueOf(15) : BigDecimal.TEN;

        // Apply minimum charge of $1.00
        if (fee.compareTo(BigDecimal.ONE) < 0) {
            fee = BigDecimal.ONE;
        }

        String message = String.format("%dh %dmin @ $%s/hr = $%s",
            duration.toHours(),
            duration.toMinutes() % 60,
            hourlyRate,
            fee);

        logger.info("Fee calculated for session {}: {}", sessionId, message);

        return FeeCalculationResponse.builder()
            .sessionId(session.getId())
            .spotNumber(spot.getSpotNumber())
            .checkInTime(session.getCheckInTime())
            .calculatedCheckOutTime(now)
            .durationMinutes(duration.toMinutes())
            .hourlyRate(hourlyRate)
            .amountDue(fee)
            .spotType(spot.getType().name())
            .message(message)
            .build();
    }

    @Override
    @CacheEvict(value = "availableSpots", allEntries = true)
    public CheckOutResponse checkOut(UUID sessionId, PaymentMethod paymentMethod) {
        logger.info("Processing checkout for session: {} with method: {}", sessionId, paymentMethod);

        ParkingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        // Prevent double checkout (idempotency protection)
        if (session.getCheckOutTime() != null) {
            logger.warn("Attempted double checkout for session: {}", sessionId);
            throw new IllegalArgumentException("Session already checked out");
        }

        try {
            OffsetDateTime now = OffsetDateTime.now();
            session.setCheckOutTime(now);

            Duration duration = Duration.between(session.getCheckInTime(), now);
            long durationMinutes = duration.toMinutes();

            // Calculate fee using strategy pattern (STANDARD, VIP rates)
            ParkingSpot spot = session.getSpot();
            String strategyKey = getStrategyKeyForSpotType(spot.getType());
            IFeeCalculationStrategy strategy = feeCalculatorFactory.getStrategy(strategyKey);
            BigDecimal fee = strategy.calculateFee(duration);

            // Enforce minimum charge of $1.00 (business rule)
            if (fee.compareTo(BigDecimal.ONE) < 0) {
                fee = BigDecimal.ONE;
            }

            session.setAmountDue(fee);
            session.setDurationMinutes(durationMinutes);

            // IMPORTANT: Set session to COMPLETED before payment processing
            // This ensures spot is freed even if payment fails
            session.setStatus(SessionStatus.COMPLETED);

            // Free up spot immediately for next user
            spot.setStatus(SpotStatus.AVAILABLE);
            spotRepository.save(spot);
            sessionRepository.save(session);

            // Process payment
            PaymentResponse paymentResponse = paymentService.processPayment(sessionId, fee, paymentMethod);

            // Log activity with audit trail
            logService.log(session.getUser(), "CHECK_OUT",
                String.format("Checked out from spot %s. Duration: %d minutes. Amount: $%s. Payment method: %s. Transaction: %s",
                    spot.getSpotNumber(), duration.toMinutes(), fee, paymentMethod.name(), paymentResponse.getTransactionId()));

            logger.info("Checkout completed successfully for session: {} - Transaction: {}", sessionId, paymentResponse.getTransactionId());

            return CheckOutResponse.builder()
                .sessionId(session.getId())
                .spotId(spot.getId())
                .spotNumber(spot.getSpotNumber())
                .checkInTime(session.getCheckInTime())
                .checkOutTime(now)
                .durationMinutes(duration.toMinutes())
                .amountDue(fee)
                .paymentId(paymentResponse.getPaymentId()) // Use actual payment ID from DB
                .paymentStatus(paymentResponse.getStatus())
                .transactionId(paymentResponse.getTransactionId())
                .paymentMethod(paymentMethod.name())
                .paidAt(OffsetDateTime.now())
                .message("Checked out and payment processed successfully")
                .build();

        } catch (Exception e) {
            logger.error("Checkout failed for session: {}", sessionId, e);
            throw new RuntimeException("Checkout processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "availableSpots", key = "'all'")
    public List<ParkingSpot> getAvailableSpots() {
        return spotRepository.findByStatusAndReservedByIsNull(SpotStatus.AVAILABLE);
    }

    @Override
    @Cacheable(value = "availableSpots", key = "{#buildingId, #floorId, #type, #status}")
    public List<ParkingSpot> getAvailableSpots(UUID buildingId, UUID floorId, SpotType type, SpotStatus status) {
        SpotStatus filterStatus = status != null ? status : SpotStatus.AVAILABLE;
        return spotRepository.findAll(
            ParkingSpotSpecification.withFilters(buildingId, floorId, type, filterStatus)
        );
    }

    @Override
    public List<ParkingSession> getActiveSessions(UUID userId) {
        logger.info("Fetching active sessions for user: {}", userId);
        return sessionRepository.findAllByUserIdAndStatusOrderByCheckInTimeDesc(userId, SessionStatus.ACTIVE);
    }

    @Override
    public List<ParkingSession> getUserSessions(UUID userId) {
        logger.info("Fetching all sessions for user: {}", userId);
        return sessionRepository.findAllByUserIdOrderByCheckInTimeDesc(userId);
    }

    /**
     * Maps spot type to fee strategy key.
     */
    private String getStrategyKeyForSpotType(SpotType spotType) {
        if (spotType == null) {
            return "STANDARD";
        }
        return switch (spotType) {
            case VIP -> "VIP";
            default -> "STANDARD";
        };
    }
}
