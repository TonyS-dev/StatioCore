package org.codeup.statiocore.service.impl;

import org.codeup.statiocore.domain.*;
import org.codeup.statiocore.domain.enums.Role;
import org.codeup.statiocore.domain.enums.SpotStatus;
import org.codeup.statiocore.exception.BadRequestException;
import org.codeup.statiocore.exception.ResourceNotFoundException;
import org.codeup.statiocore.repository.IParkingSpotRepository;
import org.codeup.statiocore.repository.IReservationRepository;
import org.codeup.statiocore.repository.IUserRepository;
import org.codeup.statiocore.service.IActivityLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReservationServiceImpl
 *
 * Tests validation rules including:
 * - Cannot create reservations in the past
 * - Spot must be available
 * - Duration must be positive
 * - Overlapping reservations are rejected
 *
 * @author TonyS-dev
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private IReservationRepository reservationRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IParkingSpotRepository spotRepository;

    @Mock
    private IActivityLogService activityLogService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User testUser;
    private ParkingSpot testSpot;
    private UUID userId;
    private UUID spotId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        spotId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .fullName("Test User")
                .role(Role.USER)
                .isActive(true)
                .build();

        testSpot = ParkingSpot.builder()
                .id(spotId)
                .spotNumber("A1")
                .status(SpotStatus.AVAILABLE)
                .build();
    }

    @Test
    void createReservation_withPastStartTime_shouldThrowBadRequestException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(spotId)).thenReturn(Optional.of(testSpot));

        // Use a time in the past (1 hour ago)
        OffsetDateTime pastTime = OffsetDateTime.now().minusHours(1);
        Integer duration = 120; // 2 hours

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            reservationService.createReservation(userId, spotId, pastTime, duration);
        });

        assertTrue(exception.getMessage().contains("Dates in the past are not available"));

        // Verify that repository save was never called
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(activityLogService, never()).log(any(), any(), any());
    }

    @Test
    void createReservation_withCurrentTime_shouldSucceed() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(spotId)).thenReturn(Optional.of(testSpot));
        when(reservationRepository.existsOverlappingReservation(any(), any(), any())).thenReturn(false);

        Reservation expectedReservation = Reservation.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .spot(testSpot)
                .build();
        when(reservationRepository.save(any(Reservation.class))).thenReturn(expectedReservation);

        // Use current time (should be valid)
        OffsetDateTime now = OffsetDateTime.now();
        Integer duration = 120;

        // Act
        Reservation result = reservationService.createReservation(userId, spotId, now, duration);

        // Assert
        assertNotNull(result);
        verify(reservationRepository).save(any(Reservation.class));
        verify(activityLogService).log(eq(testUser), eq("RESERVATION_CREATED"), anyString());
    }

    @Test
    void createReservation_withFutureTime_shouldSucceed() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(spotId)).thenReturn(Optional.of(testSpot));
        when(reservationRepository.existsOverlappingReservation(any(), any(), any())).thenReturn(false);

        Reservation expectedReservation = Reservation.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .spot(testSpot)
                .build();
        when(reservationRepository.save(any(Reservation.class))).thenReturn(expectedReservation);

        // Use a time in the future (1 hour from now)
        OffsetDateTime futureTime = OffsetDateTime.now().plusHours(1);
        Integer duration = 120;

        // Act
        Reservation result = reservationService.createReservation(userId, spotId, futureTime, duration);

        // Assert
        assertNotNull(result);
        verify(reservationRepository).save(any(Reservation.class));
        verify(activityLogService).log(eq(testUser), eq("RESERVATION_CREATED"), anyString());
    }

    @Test
    void createReservation_withUnavailableSpot_shouldThrowBadRequestException() {
        // Arrange
        testSpot.setStatus(SpotStatus.OCCUPIED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(spotId)).thenReturn(Optional.of(testSpot));

        OffsetDateTime futureTime = OffsetDateTime.now().plusHours(1);
        Integer duration = 120;

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            reservationService.createReservation(userId, spotId, futureTime, duration);
        });

        assertTrue(exception.getMessage().contains("not available"));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_withNonExistentUser_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        OffsetDateTime futureTime = OffsetDateTime.now().plusHours(1);
        Integer duration = 120;

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.createReservation(userId, spotId, futureTime, duration);
        });

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_withNegativeDuration_shouldThrowBadRequestException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(spotId)).thenReturn(Optional.of(testSpot));

        OffsetDateTime futureTime = OffsetDateTime.now().plusHours(1);
        Integer negativeDuration = -60;

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            reservationService.createReservation(userId, spotId, futureTime, negativeDuration);
        });

        assertTrue(exception.getMessage().contains("Duration must be a positive"));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_withOverlappingReservation_shouldThrowBadRequestException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(spotId)).thenReturn(Optional.of(testSpot));
        when(reservationRepository.existsOverlappingReservation(any(), any(), any())).thenReturn(true);

        OffsetDateTime futureTime = OffsetDateTime.now().plusHours(1);
        Integer duration = 120;

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            reservationService.createReservation(userId, spotId, futureTime, duration);
        });

        assertTrue(exception.getMessage().contains("overlapping reservation"));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}

