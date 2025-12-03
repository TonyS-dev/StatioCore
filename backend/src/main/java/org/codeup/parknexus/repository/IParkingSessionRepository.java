package org.codeup.parknexus.repository;

import org.codeup.parknexus.domain.ParkingSession;
import org.codeup.parknexus.domain.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IParkingSessionRepository extends JpaRepository<ParkingSession, UUID> {

    // To check if a user already has a car parked (Prevent double check-in)
       Optional<ParkingSession> findByUserIdAndStatus(UUID userId, SessionStatus status); // status = ACTIVE

    // To find the active session for a specific Spot (when trying to leave)
       Optional<ParkingSession> findBySpotIdAndStatus(UUID spotId, SessionStatus status);

    // Admin Dashboard: See how many cars are inside RIGHT NOW
    long countByStatus(SessionStatus status);

    // To get all active sessions for a user with eager loading
    @Query("SELECT s FROM ParkingSession s " +
           "LEFT JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.spot spot " +
           "LEFT JOIN FETCH spot.floor f " +
           "LEFT JOIN FETCH f.building " +
           "WHERE s.user.id = :userId AND s.status = :status " +
           "ORDER BY s.checkInTime DESC")
       List<ParkingSession> findAllByUserIdAndStatusOrderByCheckInTimeDesc(@Param("userId") UUID userId, @Param("status") SessionStatus status);

    // To get all sessions for a user (not just active) with eager loading
    @Query("SELECT s FROM ParkingSession s " +
           "LEFT JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.spot spot " +
           "LEFT JOIN FETCH spot.floor f " +
           "LEFT JOIN FETCH f.building " +
           "WHERE s.user.id = :userId " +
           "ORDER BY s.checkInTime DESC")
    List<ParkingSession> findAllByUserIdOrderByCheckInTimeDesc(@Param("userId") UUID userId);
}

