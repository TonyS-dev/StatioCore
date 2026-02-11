package org.codeup.statiocore.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.statiocore.domain.Reservation;
import org.codeup.statiocore.domain.ParkingSpot;
import org.codeup.statiocore.domain.User;
import org.codeup.statiocore.domain.enums.ReservationStatus;
import org.codeup.statiocore.domain.enums.SpotStatus;
import org.codeup.statiocore.exception.BadRequestException;
import org.codeup.statiocore.exception.ResourceNotFoundException;
import org.codeup.statiocore.repository.IReservationRepository;
import org.codeup.statiocore.repository.IParkingSpotRepository;
import org.codeup.statiocore.repository.IUserRepository;
import org.codeup.statiocore.service.IActivityLogService;
import org.codeup.statiocore.service.IReservationService;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements IReservationService {
    private final IReservationRepository reservationRepository;
    private final IParkingSpotRepository spotRepository;
    private final IUserRepository userRepository;
    private final IActivityLogService activityLogService;

    @Override
    public Reservation createReservation(UUID userId, UUID spotId, OffsetDateTime startTime, Integer durationMinutes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ParkingSpot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        if (spot.getStatus() != SpotStatus.AVAILABLE) {
            throw new BadRequestException("Spot is not available for reservation");
        }
        
        // Validate that reservation is not in the past
        OffsetDateTime now = OffsetDateTime.now();
        if (startTime.isBefore(now)) {
            throw new BadRequestException("Dates in the past are not available for reservations. Please select a future date and time.");
        }

        // Use default duration of 2 hours (120 minutes) if not provided
        if (durationMinutes == null) {
            durationMinutes = 120;
        }
        if (durationMinutes <= 0) {
            throw new BadRequestException("Duration must be a positive number of minutes");
        }
        
        OffsetDateTime endTime = startTime.plusMinutes(durationMinutes);
        boolean overlap = reservationRepository.existsOverlappingReservation(spotId, startTime, endTime);
        if (overlap) {
            throw new BadRequestException("There is an overlapping reservation for this spot and time");
        }
        Reservation reservation = Reservation.builder()
                .user(user)
                .spot(spot)
                .startTime(startTime)
                .endTime(endTime)
                .status(ReservationStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();
        reservation = reservationRepository.save(reservation);
        
        // Log reservation creation
        activityLogService.log(user, "RESERVATION_CREATED", 
            String.format("User created a new reservation for spot %s from %s to %s", 
                spot.getSpotNumber(), startTime, endTime));
        
        return reservation;
    }

    @Override
    public void cancelReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(OffsetDateTime.now());
        reservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> getUserReservations(UUID userId) {
        return reservationRepository.findByUserIdOrderByStartTimeDesc(userId);
    }
}

