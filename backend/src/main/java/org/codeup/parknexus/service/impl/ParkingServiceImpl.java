package org.codeup.parknexus.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.ParkingSession;
import org.codeup.parknexus.domain.ParkingSpot;
import org.codeup.parknexus.domain.User;
import org.codeup.parknexus.domain.enums.SpotStatus;
import org.codeup.parknexus.domain.enums.SessionStatus;
import org.codeup.parknexus.exception.ConflictException;
import org.codeup.parknexus.exception.ResourceNotFoundException;
import org.codeup.parknexus.repository.IParkingSessionRepository;
import org.codeup.parknexus.repository.IParkingSpotRepository;
import org.codeup.parknexus.service.IActivityLogService;
import org.codeup.parknexus.service.IParkingService;
import org.codeup.parknexus.service.IPaymentService;
import org.codeup.parknexus.domain.enums.PaymentMethod;
import org.codeup.parknexus.web.dto.user.CheckOutResponse;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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

    @Override
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
    public CheckOutResponse checkOut(UUID sessionId) {
        ParkingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        OffsetDateTime now = OffsetDateTime.now();
        session.setCheckOutTime(now);

        long durationMinutes = ChronoUnit.MINUTES.between(session.getCheckInTime(), now);
        BigDecimal ratePerMinute = new BigDecimal("10").divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);
        BigDecimal fee = ratePerMinute.multiply(BigDecimal.valueOf(durationMinutes)).setScale(2, RoundingMode.HALF_UP);
        session.setAmountDue(fee);
        session.setStatus(SessionStatus.COMPLETED);

        ParkingSpot spot = session.getSpot();
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
                .durationMinutes(durationMinutes)
                .fee(fee.doubleValue())
                .build();
    }

    @Override
    public List<ParkingSpot> getAvailableSpots() {
        return spotRepository.findByStatus(SpotStatus.AVAILABLE);
    }
}
