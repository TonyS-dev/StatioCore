package org.codeup.statiocore.service;

import org.codeup.statiocore.domain.Reservation;
import org.codeup.statiocore.domain.User;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface IReservationService {
    Reservation createReservation(UUID userId, UUID spotId, OffsetDateTime startTime, Integer durationMinutes);
    void cancelReservation(UUID reservationId);
    List<Reservation> getUserReservations(UUID userId);
}
