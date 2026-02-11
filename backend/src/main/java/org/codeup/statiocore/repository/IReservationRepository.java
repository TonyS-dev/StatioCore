package org.codeup.statiocore.repository;

import org.codeup.statiocore.domain.Reservation;
import org.codeup.statiocore.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IReservationRepository extends JpaRepository<Reservation, UUID> {

    // For the user to see "My Reservations"
    List<Reservation> findByUserIdOrderByStartTimeDesc(UUID userId);

    // To find active/pending reservations
    List<Reservation> findByStatus(ReservationStatus status);

    // CRITICAL: Overlap Validation
    // Checks if an active reservation already exists for that spot in that time range
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.spot.id = :spotId
        AND r.status IN ('PENDING', 'ACTIVE')
        AND (r.startTime < :endTime AND r.endTime > :startTime)
    """)
    boolean existsOverlappingReservation(
            @Param("spotId") UUID spotId,
            @Param("startTime") OffsetDateTime start,
            @Param("endTime") OffsetDateTime end
    );
}
