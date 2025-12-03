package org.codeup.parknexus.service;

import org.codeup.parknexus.domain.ParkingSession;
import org.codeup.parknexus.domain.ParkingSpot;
import org.codeup.parknexus.domain.User;
import org.codeup.parknexus.domain.enums.PaymentMethod;
import org.codeup.parknexus.domain.enums.SpotStatus;
import org.codeup.parknexus.domain.enums.SpotType;
import org.codeup.parknexus.web.dto.user.CheckOutResponse;
import org.codeup.parknexus.web.dto.user.FeeCalculationResponse;

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

