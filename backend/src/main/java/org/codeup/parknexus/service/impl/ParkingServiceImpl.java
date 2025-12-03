package org.codeup.parknexus.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.ParkingSession;
import org.codeup.parknexus.domain.ParkingSpot;
import org.codeup.parknexus.domain.User;
import org.codeup.parknexus.domain.enums.SpotStatus;
import org.codeup.parknexus.domain.enums.SessionStatus;
import org.codeup.parknexus.domain.enums.SpotType;
import org.codeup.parknexus.exception.ConflictException;
import org.codeup.parknexus.exception.ResourceNotFoundException;
import org.codeup.parknexus.repository.IParkingSessionRepository;
import org.codeup.parknexus.repository.IParkingSpotRepository;
import org.codeup.parknexus.repository.specification.ParkingSpotSpecification;
import org.codeup.parknexus.service.IActivityLogService;
import org.codeup.parknexus.service.IParkingService;
import org.codeup.parknexus.service.IPaymentService;
import org.codeup.parknexus.service.strategy.FeeCalculatorFactory;
import org.codeup.parknexus.service.strategy.IFeeCalculationStrategy;
import org.codeup.parknexus.domain.enums.PaymentMethod;
import org.codeup.parknexus.web.dto.user.CheckOutResponse;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ParkingServiceImpl implements IParkingService {
    private final IParkingSpotRepository spotRepository;
    private final IParkingSessionRepository sessionRepository;
    private final IActivityLogService logService;
    private final IPaymentService paymentService;
    private final FeeCalculatorFactory feeCalculatorFactory;

    @Override
    @CacheEvict(value = "availableSpots", allEntries = true)
    public ParkingSession checkIn(User user, UUID spotId) {
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
                .status(SessionStatus.ACTIVE)
                .build();

        session = sessionRepository.save(session);
        logService.log(user, "CHECK_IN", "Spot: " + spot.getSpotNumber());

        return session;
    }

    @Override
    @CacheEvict(value = "availableSpots", allEntries = true)
    public CheckOutResponse checkOut(UUID sessionId) {
        ParkingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        OffsetDateTime now = OffsetDateTime.now();
        session.setCheckOutTime(now);

        Duration duration = Duration.between(session.getCheckInTime(), now);

        // Select fee strategy based on spot type
        ParkingSpot spot = session.getSpot();
        String strategyKey = getStrategyKeyForSpotType(spot.getType());
        IFeeCalculationStrategy strategy = feeCalculatorFactory.getStrategy(strategyKey);
        BigDecimal fee = strategy.calculateFee(duration);

        session.setAmountDue(fee);
        session.setStatus(SessionStatus.COMPLETED);

        spot.setStatus(SpotStatus.AVAILABLE);
        spotRepository.save(spot);

        sessionRepository.save(session);

        // create payment record (currency USD by default, method CASH for now)
        paymentService.createPayment(session, fee, PaymentMethod.CASH, "USD");

        logService.log(session.getUser(), "CHECK_OUT", "Fee: $" + fee);

        return CheckOutResponse.builder()
                .sessionId(session.getId())
                .spotId(spot.getId())
                .checkInTime(session.getCheckInTime().toLocalDateTime())
                .checkOutTime(now.toLocalDateTime())
                .durationMinutes(duration.toMinutes())
                .fee(fee.doubleValue())
                .build();
    }

    @Override
    @Cacheable(value = "availableSpots", key = "'all'")
    public List<ParkingSpot> getAvailableSpots() {
        return spotRepository.findByStatus(SpotStatus.AVAILABLE);
    }

    @Override
    @Cacheable(value = "availableSpots", key = "{#buildingId, #floorId, #type, #status}")
    public List<ParkingSpot> getAvailableSpots(UUID buildingId, UUID floorId, SpotType type, SpotStatus status) {
        SpotStatus filterStatus = status != null ? status : SpotStatus.AVAILABLE;
        return spotRepository.findAll(
            ParkingSpotSpecification.withFilters(buildingId, floorId, type, filterStatus)
        );
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
