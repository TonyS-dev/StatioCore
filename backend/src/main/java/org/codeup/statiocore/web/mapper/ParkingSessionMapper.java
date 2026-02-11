package org.codeup.statiocore.web.mapper;

import org.codeup.statiocore.domain.ParkingSession;
import org.codeup.statiocore.web.dto.user.ParkingSessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ParkingSpotMapper.class})
public interface ParkingSessionMapper {
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "spotId", source = "spot.id")
    @Mapping(target = "spotNumber", source = "spot.spotNumber")
    @Mapping(target = "buildingName", source = "spot.floor.building.name")
    @Mapping(target = "floorNumber", source = "spot.floor.floorNumber")
    @Mapping(target = "spot", source = "spot")
    @Mapping(target = "vehicleNumber", source = "vehicleNumber")
    @Mapping(target = "status", expression = "java(session.getStatus().name())")
    @Mapping(target = "duration", expression = "java(calculateDuration(session))")
    @Mapping(target = "fee", source = "amountDue")
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    ParkingSessionResponse toResponse(ParkingSession session);
    
    List<ParkingSessionResponse> toResponses(List<ParkingSession> sessions);
    
    // Helper method to calculate duration in minutes
    default Long calculateDuration(ParkingSession session) {
        if (session.getCheckInTime() == null) {
            return null;
        }

        // Use stored duration if available (for completed sessions)
        if (session.getDurationMinutes() != null && session.getDurationMinutes() > 0) {
            return session.getDurationMinutes();
        }

        // For active sessions, calculate duration from check-in to now
        if (session.getStatus().name().equals("ACTIVE")) {
            return java.time.temporal.ChronoUnit.MINUTES.between(
                session.getCheckInTime(),
                java.time.OffsetDateTime.now()
            );
        }

        // For completed sessions without stored duration, calculate from check-in to check-out
        if (session.getCheckOutTime() != null) {
            return java.time.temporal.ChronoUnit.MINUTES.between(
                session.getCheckInTime(),
                session.getCheckOutTime()
            );
        }

        return null;
    }
}
