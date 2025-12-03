package org.codeup.parknexus.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.ParkingSession;
import org.codeup.parknexus.domain.Reservation;
import org.codeup.parknexus.domain.enums.ReservationStatus;
import org.codeup.parknexus.domain.enums.SessionStatus;
import org.codeup.parknexus.domain.enums.SpotStatus;
import org.codeup.parknexus.repository.IParkingSessionRepository;
import org.codeup.parknexus.repository.IParkingSpotRepository;
import org.codeup.parknexus.repository.IReservationRepository;
import org.codeup.parknexus.service.IUserService;
import org.codeup.parknexus.web.dto.user.DashboardResponse;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements IUserService {
    private final IReservationRepository reservationRepository;
    private final IParkingSessionRepository sessionRepository;
    private final IParkingSpotRepository spotRepository;

    @Override
    @Cacheable(value = "userDashboard", key = "#userId")
    public DashboardResponse getDashboard(UUID userId) {
        // Get user's reservations
        List<Reservation> reservations = reservationRepository.findByUserIdOrderByStartTimeDesc(userId);
        
        // Get user's active sessions
        List<ParkingSession> activeSessions = sessionRepository.findAllByUserIdAndStatusOrderByCheckInTimeDesc(userId, SessionStatus.ACTIVE);
        
        // Get user's all sessions for statistics
        List<ParkingSession> allSessions = sessionRepository.findAllByUserIdOrderByCheckInTimeDesc(userId);
        
        // Calculate spot statistics (system-wide)
        long totalSpots = spotRepository.count();
        long occupiedSpots = spotRepository.countByStatus(SpotStatus.OCCUPIED);
        // Strict AVAILABLE: exclude reserved spots
        long availableSpots = spotRepository.countByStatusAndReservedByIsNull(SpotStatus.AVAILABLE);
        double occupancyPercentage = totalSpots > 0 ? (occupiedSpots * 100.0 / totalSpots) : 0.0;
        
        // Calculate user reservations stats
        long activeReservations = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE || r.getStatus() == ReservationStatus.PENDING)
                .count();
        long totalReservations = reservations.size();
        
        // Calculate user sessions stats
        long completedSessions = allSessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                .count();
        
        // Calculate financial stats
        BigDecimal totalEarnings = allSessions.stream()
                .filter(s -> s.getAmountDue() != null)
                .map(ParkingSession::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageSessionFee = completedSessions > 0 
            ? totalEarnings.divide(BigDecimal.valueOf(completedSessions), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        // Build recent activity
        List<DashboardResponse.ActivityRecord> recentActivity = buildRecentActivity(allSessions, reservations);
        
        return DashboardResponse.builder()
                .totalSpots(totalSpots)
                .availableSpots(availableSpots)
                .occupiedSpots(occupiedSpots)
                .occupancyPercentage(occupancyPercentage)
                .activeReservations(activeReservations)
                .activeSessions((long) activeSessions.size())
                .totalReservations(totalReservations)
                .totalCompletedSessions(completedSessions)
                .totalEarnings(totalEarnings)
                .outstandingFees(BigDecimal.ZERO)
                .averageSessionFee(averageSessionFee)
                .recentActivity(recentActivity)
                .build();
    }
    
    private List<DashboardResponse.ActivityRecord> buildRecentActivity(
            List<ParkingSession> sessions, 
            List<Reservation> reservations) {
        List<DashboardResponse.ActivityRecord> activity = new ArrayList<>();
        
        // Add recent sessions
        sessions.stream()
                .limit(3)
                .forEach(session -> {
                    String action = session.getStatus().name().equals("ACTIVE") ? "Checked In" : "Checked Out";
                    String details = "Spot " + session.getSpot().getSpotNumber() + 
                            " at " + session.getSpot().getFloor().getBuilding().getName();
                    activity.add(DashboardResponse.ActivityRecord.builder()
                            .action(action)
                            .details(details)
                            .timestamp(session.getCheckInTime().format(DateTimeFormatter.ISO_DATE_TIME))
                            .build());
                });
        
        // Add recent reservations
        reservations.stream()
                .limit(2)
                .forEach(reservation -> {
                    activity.add(DashboardResponse.ActivityRecord.builder()
                            .action("Reserved")
                            .details("Spot " + reservation.getSpot().getSpotNumber() + 
                                    " at " + reservation.getSpot().getFloor().getBuilding().getName())
                            .timestamp(reservation.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .build());
                });
        
        // Sort by timestamp (most recent first)
        activity.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        return activity.stream().limit(5).collect(Collectors.toList());
    }
}
