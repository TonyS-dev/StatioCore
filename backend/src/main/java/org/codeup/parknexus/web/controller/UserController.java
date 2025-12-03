package org.codeup.parknexus.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.ParkingSession;
import org.codeup.parknexus.domain.ParkingSpot;
import org.codeup.parknexus.domain.Reservation;
import org.codeup.parknexus.domain.enums.PaymentMethod;
import org.codeup.parknexus.domain.enums.SpotStatus;
import org.codeup.parknexus.domain.enums.SpotType;
import org.codeup.parknexus.exception.ResourceNotFoundException;
import org.codeup.parknexus.service.IReservationService;
import org.codeup.parknexus.service.IParkingService;
import org.codeup.parknexus.service.IPaymentService;
import org.codeup.parknexus.service.IUserService;
import org.codeup.parknexus.service.IAdminService;
import org.codeup.parknexus.web.dto.user.*;
import org.codeup.parknexus.web.mapper.ParkingSpotMapper;
import org.codeup.parknexus.web.mapper.ReservationMapper;
import org.codeup.parknexus.web.mapper.ParkingSessionMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * User API Controller with comprehensive Swagger documentation.
 *
 * This controller handles all user-facing endpoints for parking operations including:
 * - User dashboard with statistics and activity logs
 * - Parking spot discovery with advanced filtering (building, floor, type)
 * - Parking session management (check-in, check-out, fee calculation)
 * - Reservation creation and management
 * - Payment processing and simulation
 *
 * Security:
 * - All endpoints require JWT Bearer token authentication except /buildings
 * - User ID extracted from JWT claims via @AuthenticationPrincipal
 * - Each request is validated through Spring Security filter chain
 *
 * Business Rules:
 * - Available spots = status AVAILABLE AND reservedBy IS NULL
 * - Users can have only ONE active parking session at a time
 * - Check-out automatically processes payment and frees the spot
 * - Minimum parking fee is $1.00 regardless of duration
 *
 * @author TonyS-dev
 * @version 1.0.0
 * @since 2025-12-05
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(
    name = "User API",
    description = "User-facing endpoints for parking operations, reservations, and payment"
)
public class UserController {

    private final IUserService userService;
    private final IParkingService parkingService;
    private final IReservationService reservationService;
    private final IPaymentService paymentService;
    private final IAdminService adminService;
    private final ParkingSpotMapper parkingSpotMapper;
    private final ReservationMapper reservationMapper;
    private final ParkingSessionMapper parkingSessionMapper;

    /**
     * Get authenticated user's dashboard with comprehensive parking statistics.
     */
    @GetMapping("/user/dashboard")
    @Operation(summary = "Get user dashboard", description = "Retrieve dashboard with parking statistics, reservations, and recent activity")
    @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - invalid JWT token")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<DashboardResponse> dashboard(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(userService.getDashboard(userId));
    }

    /**
     * Get all buildings for parking spot search filtering (Public endpoint).
     */
    @GetMapping("/buildings")
    @Operation(summary = "Get all buildings", description = "Retrieve all buildings with floor and spot counts for filtering. Public endpoint.")
    @ApiResponse(responseCode = "200", description = "Buildings retrieved successfully")
    public ResponseEntity<List<org.codeup.parknexus.web.dto.admin.BuildingResponse>> getBuildings() {
        return ResponseEntity.ok(adminService.getAllBuildings());
    }

    /**
     * Search for available parking spots with flexible filtering.
     */
    @GetMapping("/spots/available")
    @Operation(summary = "Search available parking spots", description = "Find available parking spots with optional filtering by building, floor, or type")
    @ApiResponse(responseCode = "200", description = "Available spots retrieved")
    @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ParkingSpotResponse>> availableSpots(
            @Parameter(description = "Filter by building UUID")
            @RequestParam(required = false) UUID buildingId,
            @Parameter(description = "Filter by floor UUID")
            @RequestParam(required = false) UUID floorId,
            @Parameter(description = "Filter by spot type (REGULAR, VIP, EV_CHARGING, HANDICAP)")
            @RequestParam(required = false) SpotType type,
            @Parameter(description = "Filter by status (default: AVAILABLE)")
            @RequestParam(required = false, defaultValue = "AVAILABLE") SpotStatus status) {
        List<ParkingSpot> spots = parkingService.getAvailableSpots(buildingId, floorId, type, status);
        return ResponseEntity.ok(parkingSpotMapper.toUserResponses(spots));
    }

    /**
     * Create a parking spot reservation for future use.
     */
    @PostMapping("/reservations")
    @Operation(summary = "Create parking spot reservation", description = "Reserve a parking spot for a specific time period")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reservation created"),
        @ApiResponse(responseCode = "400", description = "Invalid reservation (spot unavailable, invalid time)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request, @AuthenticationPrincipal UUID userId) {
        Reservation reservation = reservationService.createReservation(
                userId,
                request.getSpotId(),
                request.getStartTime(),
                request.getDurationMinutes()
        );
        return ResponseEntity.ok(reservationMapper.toResponse(reservation));
    }

    /**
     * Check-in to a parking spot and start a parking session.
     */
    @PostMapping("/parking/check-in")
    @Operation(summary = "Check-in to parking spot", description = "Start a parking session at a specific spot")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check-in successful"),
        @ApiResponse(responseCode = "400", description = "Spot not available or user has active session"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Spot not found")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<CheckInResponse> checkIn(@Valid @RequestBody CheckInRequest request, @AuthenticationPrincipal UUID userId) {
        var session = parkingService.checkIn(userId, request.getSpotId(), request.getVehicleNumber());
        CheckInResponse resp = CheckInResponse.builder()
                .sessionId(session.getId())
                .spotId(session.getSpot().getId())
                .spotNumber(session.getSpot().getSpotNumber())
                .vehicleNumber(session.getVehicleNumber())
                .buildingName(session.getSpot().getFloor().getBuilding().getName())
                .floorName("Floor " + session.getSpot().getFloor().getFloorNumber())
                .checkInTime(session.getCheckInTime())
                .status(session.getStatus().name())
                .message("Checked in successfully")
                .build();
        return ResponseEntity.ok(resp);
    }

    /**
     * Calculate parking fee for active session (preview before checkout).
     */
    @PostMapping("/parking/calculate-fee")
    @Operation(summary = "Calculate parking fee preview", description = "Calculate estimated fee without finalizing payment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fee calculated"),
        @ApiResponse(responseCode = "400", description = "Invalid session or already checked out"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<FeeCalculationResponse> calculateFee(@RequestParam UUID sessionId) {
        try {
            FeeCalculationResponse response = parkingService.calculateFee(sessionId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check-out from parking spot and process payment.
     */
    @PostMapping("/parking/check-out")
    @Operation(summary = "Check-out from parking spot", description = "End parking session, calculate final fee, and process payment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check-out successful, payment processed"),
        @ApiResponse(responseCode = "400", description = "Session already checked out or payment failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "500", description = "Payment processing error")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<CheckOutResponse> checkOut(
            @RequestParam UUID sessionId,
            @RequestParam(required = false, defaultValue = "CREDIT_CARD") PaymentMethod paymentMethod) {
        try {
            CheckOutResponse response = parkingService.checkOut(sessionId, paymentMethod);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get list of user's active parking sessions.
     */
    @GetMapping("/parking/sessions/active")
    @Operation(summary = "Get active parking sessions", description = "Retrieve all current active parking sessions")
    @ApiResponse(responseCode = "200", description = "Active sessions retrieved")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ParkingSessionResponse>> getActiveSessions(@AuthenticationPrincipal UUID userId) {
        List<ParkingSession> sessions = parkingService.getActiveSessions(userId);
        return ResponseEntity.ok(parkingSessionMapper.toResponses(sessions));
    }

    /**
     * Get all parking sessions history for user (active and completed).
     */
    @GetMapping("/parking/sessions/my")
    @Operation(summary = "Get user's parking sessions history", description = "Retrieve complete history of all parking sessions")
    @ApiResponse(responseCode = "200", description = "Sessions retrieved")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ParkingSessionResponse>> getMySessions(@AuthenticationPrincipal UUID userId) {
        List<ParkingSession> sessions = parkingService.getUserSessions(userId);
        return ResponseEntity.ok(parkingSessionMapper.toResponses(sessions));
    }

    /**
     * Get user's parking spot reservations.
     */
    @GetMapping("/reservations")
    @Operation(summary = "Get user's reservations", description = "Retrieve all parking spot reservations for authenticated user")
    @ApiResponse(responseCode = "200", description = "Reservations retrieved")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ReservationResponse>> getReservations(@AuthenticationPrincipal UUID userId) {
        List<Reservation> reservations = reservationService.getUserReservations(userId);
        return ResponseEntity.ok(reservations.stream()
                .map(reservationMapper::toResponse)
                .toList());
    }

    /**
     * Cancel a parking spot reservation.
     */
    @DeleteMapping("/reservations/{reservationId}")
    @Operation(summary = "Cancel reservation", description = "Cancel a parking spot reservation")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reservation cancelled"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel expired reservation"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<Void> cancelReservation(
            @Parameter(description = "Reservation UUID")
            @PathVariable UUID reservationId,
            @AuthenticationPrincipal UUID userId) {
        try {
            reservationService.cancelReservation(reservationId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Simulate payment for a parking session (for testing/demo purposes).
     */
    @PostMapping("/parking/payment/simulate")
    @Operation(summary = "Simulate payment", description = "Process simulated payment for parking session (for testing/demo purposes)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid amount or payment failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "500", description = "Payment processing error")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<PaymentResponse> simulatePayment(
            @Parameter(description = "Parking session UUID")
            @RequestParam UUID sessionId,
            @Parameter(description = "Payment amount in USD")
            @RequestParam BigDecimal amount) {
        try {
            PaymentResponse response = paymentService.processPayment(sessionId, amount, PaymentMethod.CREDIT_CARD);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
