package org.codeup.parknexus.service;

import org.codeup.parknexus.domain.Reservation;
import org.codeup.parknexus.domain.User;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface IReservationService {
    Reservation createReservation(User user, UUID spotId, OffsetDateTime startTime, Integer durationMinutes);
    void cancelReservation(UUID reservationId);
    List<Reservation> getUserReservations(UUID userId);
}
