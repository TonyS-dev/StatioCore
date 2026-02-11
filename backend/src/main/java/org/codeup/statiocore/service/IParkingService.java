package org.codeup.statiocore.service;

import org.codeup.statiocore.domain.ParkingSession;
import org.codeup.statiocore.domain.ParkingSpot;
import org.codeup.statiocore.domain.User;
import org.codeup.statiocore.domain.enums.PaymentMethod;
import org.codeup.statiocore.domain.enums.SpotStatus;
import org.codeup.statiocore.domain.enums.SpotType;
import org.codeup.statiocore.web.dto.user.CheckOutResponse;
import org.codeup.statiocore.web.dto.user.FeeCalculationResponse;

import java.util.List;
import java.util.UUID;

public interface IParkingService {
    ParkingSession checkIn(UUID userId, UUID spotId, String vehicleNumber);

    FeeCalculationResponse calculateFee(UUID sessionId);

    CheckOutResponse checkOut(UUID sessionId, PaymentMethod paymentMethod);

    List<ParkingSpot> getAvailableSpots();

    List<ParkingSpot> getAvailableSpots(UUID buildingId, UUID floorId, SpotType type, SpotStatus status);

    List<ParkingSession> getActiveSessions(UUID userId);

    List<ParkingSession> getUserSessions(UUID userId);
}

