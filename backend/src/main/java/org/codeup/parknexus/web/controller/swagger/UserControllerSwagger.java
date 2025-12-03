package org.codeup.parknexus.web.controller.swagger;

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
 * User API Controller with Swagger Documentation
 *
 * This controller handles all user-facing endpoints for parking operations including:
 * - User dashboard with statistics and activity logs
 * - Parking spot discovery with advanced filtering (building, floor, type)
 * - Parking session management (check-in, check-out, fee calculation)
 * - Reservation creation and management
 * - Payment processing
 *
 * All endpoints require JWT Bearer token authentication.
 * Status codes: 200 (success), 400 (bad request), 401 (unauthorized), 404 (not found), 500 (server error)
 *
 * @author TonyS-dev
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(
    name = "User API",
    description = "User-facing endpoints for parking operations, reservations, and payment"
)
public class UserControllerSwagger {

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
     *
     * Returns dashboard containing:
     * - Total, available, and occupied parking spots system-wide
     * - User's active and total reservations
     * - User's active and completed parking sessions
     * - User's financial statistics (total earnings, average fee, outstanding fees)
     * - Recent activity log (check-ins, check-outs, reservations)
     *
     * @param userId Authenticated user ID (injected from JWT token via SecurityContext)
     * @return DashboardResponse with parking statistics and recent activity
     * @example GET /api/user/dashboard
     *          Headers: Authorization: Bearer {jwt_token}
     *          Response: { "totalSpots": 100, "availableSpots": 45, ... }
     */
    @GetMapping("/user/dashboard")
    @Operation(
        summary = "Get user dashboard",
        description = "Retrieve dashboard with parking statistics, reservations, sessions, and recent activity"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully",
            content = @Content(schema = @Schema(implementation = DashboardResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<DashboardResponse> dashboard(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(userService.getDashboard(userId));
    }

    /**
     * Get list of all buildings for parking spot search and filtering.
     *
     * Public endpoint (no auth required). Returns all buildings with their floor and spot counts.
     * Used by frontend for building dropdown/filter options.
     *
     * @return List of BuildingResponse containing building info and parking statistics
     * @example GET /api/buildings
     *          Response: [{ "id": "uuid", "name": "Downtown Tower", "totalFloors": 3, "totalSpots": 50 }, ...]
     */
    @GetMapping("/buildings")
    @Operation(
        summary = "Get all buildings",
        description = "Retrieve all buildings with floor and parking spot counts. Public endpoint."
    )
    @ApiResponse(responseCode = "200", description = "Buildings retrieved successfully",
        content = @Content(schema = @Schema(implementation = List.class)))
    public ResponseEntity<List<org.codeup.parknexus.web.dto.admin.BuildingResponse>> getBuildings() {
        // Public endpoint to get all buildings for filtering in UI
        return ResponseEntity.ok(adminService.getAllBuildings());
    }

    /**
     * Search for available parking spots with flexible filtering.
     *
     * Returns strictly AVAILABLE spots (status AVAILABLE and not reserved). Supports filtering by:
     * - Building ID: narrow down to specific building
     * - Floor ID: show spots on specific floor
     * - Spot Type: filter by REGULAR, VIP, EV_CHARGING, HANDICAP
     * - Status: defaults to AVAILABLE
     *
     * When multiple filters are provided, results are AND-ed together.
     * Empty result set means no spots match the filter criteria.
     *
     * @param buildingId Optional: filter by building UUID
     * @param floorId Optional: filter by floor UUID
     * @param type Optional: filter by SpotType (REGULAR, VIP, EV_CHARGING, HANDICAP)
     * @param status Optional: filter by spot status (defaults to AVAILABLE)
     * @return List of available ParkingSpotResponse matching filter criteria
     * @example GET /api/spots/available?buildingId={uuid}&type=VIP
     *          Response: [{ "id": "uuid", "spotNumber": "A1", "type": "VIP", "status": "AVAILABLE" }, ...]
     */
    @GetMapping("/spots/available")
    @Operation(
        summary = "Search available parking spots",
        description = "Find available parking spots with optional filtering by building, floor, or type. All results are strictly AVAILABLE (not RESERVED or OCCUPIED)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Available spots retrieved successfully",
            content = @Content(schema = @Schema(implementation = ParkingSpotResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ParkingSpotResponse>> availableSpots(
            @Parameter(description = "Filter by building UUID", example = "650e8400-e29b-41d4-a716-446655440001")
            @RequestParam(required = false) UUID buildingId,
            @Parameter(description = "Filter by floor UUID")
            @RequestParam(required = false) UUID floorId,
            @Parameter(description = "Filter by spot type: REGULAR, VIP, EV_CHARGING, HANDICAP")
            @RequestParam(required = false) SpotType type,
            @Parameter(description = "Filter by spot status (default: AVAILABLE)")
            @RequestParam(required = false, defaultValue = "AVAILABLE") SpotStatus status) {
        List<ParkingSpot> spots = parkingService.getAvailableSpots(buildingId, floorId, type, status);
        return ResponseEntity.ok(parkingSpotMapper.toUserResponses(spots));
    }

    /**
     * Create a parking spot reservation for future use.
     *
     * Reserves a specific parking spot for a future time period. The spot becomes unavailable
     * to other users from startTime until endTime (= startTime + durationMinutes).
     * Reservations must be at least 30 minutes in advance.
     *
     * Business Rules:
     * - Only AVAILABLE spots can be reserved
     * - One active reservation per user per building at a time
     * - Reservation status: PENDING → ACTIVE → COMPLETED/CANCELLED
     * - User can check-in only during active reservation time window
     *
     * @param request ReservationRequest with spotId, startTime, durationMinutes
     * @param userId Authenticated user ID (from JWT token)
     * @return ReservationResponse with reservation ID and details
     * @example POST /api/reservations
     *          Body: { "spotId": "uuid", "startTime": "2025-12-04T10:00:00", "durationMinutes": 120 }
     *          Response: { "id": "uuid", "status": "PENDING", "createdAt": "..." }
     */
    @PostMapping("/reservations")
    @Operation(
        summary = "Create parking spot reservation",
        description = "Reserve a specific parking spot for a future time period. Spot becomes unavailable to others during reservation window."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reservation created successfully",
            content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid reservation request (spot not available, invalid time, etc.)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ReservationResponse> reserve(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal UUID userId) {
        Reservation reservation = reservationService.createReservation(
                userId,
                request.getSpotId(),
                request.getStartTime(),
                request.getDurationMinutes()
        );
        return ResponseEntity.ok(reservationMapper.toResponse(reservation));
    }

    /**
     * Check-in to a parking spot and start parking session.
     *
     * Initiates a parking session when user arrives at the parking spot. The spot's status
     * changes from AVAILABLE to OCCUPIED. Session tracking begins immediately.
     * Parking fees accrue from check-in time onwards.
     *
     * Business Rules:
     * - Only AVAILABLE spots can be checked into
     * - User can have only one ACTIVE session at a time
     * - Vehicle number is recorded for tracking
     * - Check-in time is the session's start timestamp
     *
     * @param request CheckInRequest with spotId and optional vehicleNumber
     * @param userId Authenticated user ID (from JWT token)
     * @return CheckInResponse with sessionId, spot details, and status
     * @example POST /api/parking/check-in
     *          Body: { "spotId": "uuid", "vehicleNumber": "ABC-123" }
     *          Response: { "sessionId": "uuid", "spotNumber": "A1", "checkInTime": "...", "status": "ACTIVE" }
     */
    @PostMapping("/parking/check-in")
    @Operation(
        summary = "Check-in to parking spot",
        description = "Start a parking session at a specific parking spot. Spot becomes OCCUPIED and fees begin accruing."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check-in successful, session created",
            content = @Content(schema = @Schema(implementation = CheckInResponse.class))),
        @ApiResponse(responseCode = "400", description = "Spot not available or user already has active session"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Spot not found")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<CheckInResponse> checkIn(
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal UUID userId) {
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
     *
     * Provides a fee preview for the current parking session without finalizing checkout.
     * Useful for users to see estimated cost before completing payment.
     *
     * Fee Calculation Logic:
     * - Based on spot type (REGULAR: $10/hr, VIP: $15/hr)
     * - Minimum charge: $1.00
     * - Duration: time from check-in to now
     * - Result: breakdown with duration, hourly rate, and total amount
     *
     * @param sessionId UUID of active parking session
     * @return FeeCalculationResponse with duration, rate, and calculated fee
     * @example POST /api/parking/calculate-fee?sessionId={uuid}
     *          Response: { "durationMinutes": 30, "hourlyRate": 10, "amountDue": 5.00, "message": "0h 30min @ $10/hr = $5.00" }
     */
    @PostMapping("/parking/calculate-fee")
    @Operation(
        summary = "Calculate parking fee preview",
        description = "Calculate estimated parking fee for active session based on duration and spot type. Does not finalize payment."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fee calculated successfully",
            content = @Content(schema = @Schema(implementation = FeeCalculationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid session or session already checked out"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<FeeCalculationResponse> calculateFee(
            @Parameter(description = "Parking session UUID")
            @RequestParam UUID sessionId) {
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
     *
     * Finalizes the parking session:
     * 1. Calculates final fee based on actual duration
     * 2. Applies payment via specified method
     * 3. Frees up the parking spot (returns to AVAILABLE status)
     * 4. Marks session as COMPLETED
     * 5. Records transaction in activity log
     *
     * Business Rules:
     * - Session must be ACTIVE (not already checked out)
     * - Minimum fee: $1.00
     * - Spot immediately becomes available after checkout
     * - Payment is simulated (no real charge)
     *
     * @param sessionId UUID of active parking session
     * @param paymentMethod Payment method (default: CREDIT_CARD; also supports: DEBIT_CARD, MOBILE_PAYMENT)
     * @return CheckOutResponse with final amounts, payment status, and transaction ID
     * @example POST /api/parking/check-out?sessionId={uuid}&paymentMethod=CREDIT_CARD
     *          Response: { "sessionId": "uuid", "amountDue": 5.00, "paymentStatus": "SUCCESS", "transactionId": "TXN-12345" }
     */
    @PostMapping("/parking/check-out")
    @Operation(
        summary = "Check-out from parking spot",
        description = "End parking session, calculate final fee, process payment, and free up parking spot"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check-out successful, payment processed",
            content = @Content(schema = @Schema(implementation = CheckOutResponse.class))),
        @ApiResponse(responseCode = "400", description = "Session already checked out or payment failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "500", description = "Payment processing error")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<CheckOutResponse> checkOut(
            @Parameter(description = "Parking session UUID")
            @RequestParam UUID sessionId,
            @Parameter(description = "Payment method (default: CREDIT_CARD)", example = "CREDIT_CARD")
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
     *
     * Returns all sessions where status = ACTIVE (currently parking).
     * Useful for users to view their current parking activity.
     *
     * @param userId Authenticated user ID (from JWT token)
     * @return List of active ParkingSessionResponse
     */
    @GetMapping("/parking/sessions/active")
    @Operation(
        summary = "Get active parking sessions",
        description = "Retrieve all current active parking sessions for authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Active sessions retrieved",
        content = @Content(schema = @Schema(implementation = ParkingSessionResponse.class)))
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ParkingSessionResponse>> getActiveSessions(@AuthenticationPrincipal UUID userId) {
        List<ParkingSession> sessions = parkingService.getActiveSessions(userId);
        return ResponseEntity.ok(parkingSessionMapper.toResponses(sessions));
    }

    /**
     * Get all parking sessions for user (active and completed).
     *
     * Returns complete history of user's parking sessions ordered by most recent first.
     * Includes ACTIVE (current) and COMPLETED (past) sessions.
     *
     * @param userId Authenticated user ID (from JWT token)
     * @return List of all ParkingSessionResponse for user
     */
    @GetMapping("/parking/sessions/my")
    @Operation(
        summary = "Get user's parking sessions history",
        description = "Retrieve complete history of all parking sessions (active and completed) for authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Sessions retrieved",
        content = @Content(schema = @Schema(implementation = ParkingSessionResponse.class)))
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ParkingSessionResponse>> getMySessions(@AuthenticationPrincipal UUID userId) {
        List<ParkingSession> sessions = parkingService.getUserSessions(userId);
        return ResponseEntity.ok(parkingSessionMapper.toResponses(sessions));
    }

    /**
     * Get user's parking spot reservations.
     *
     * Returns all reservations made by user (PENDING, ACTIVE, COMPLETED, CANCELLED).
     * Ordered by most recent first.
     *
     * @param userId Authenticated user ID (from JWT token)
     * @return List of ReservationResponse
     */
    @GetMapping("/reservations")
    @Operation(
        summary = "Get user's reservations",
        description = "Retrieve all parking spot reservations for authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Reservations retrieved",
        content = @Content(schema = @Schema(implementation = ReservationResponse.class)))
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ReservationResponse>> getReservations(@AuthenticationPrincipal UUID userId) {
        List<Reservation> reservations = reservationService.getUserReservations(userId);
        return ResponseEntity.ok(reservations.stream()
                .map(reservationMapper::toResponse)
                .toList());
    }

    /**
     * Cancel a parking spot reservation.
     *
     * Cancels a user's reservation and frees up the spot for others.
     * Can only cancel reservations that haven't started yet (PENDING or ACTIVE status).
     *
     * @param reservationId UUID of reservation to cancel
     * @param userId Authenticated user ID (from JWT token)
     * @return 204 No Content on success
     */
    @DeleteMapping("/reservations/{reservationId}")
    @Operation(
        summary = "Cancel reservation",
        description = "Cancel a parking spot reservation and free up the spot"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reservation cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel completed or expired reservation"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<Void> cancelReservation(
            @Parameter(description = "Reservation UUID to cancel")
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
     * Simulate payment for a parking session.
     *
     * Used for testing and demonstrations. In production, this would integrate with
     * actual payment gateways. Currently simulates successful payment processing.
     *
     * @param sessionId UUID of parking session
     * @param amount Payment amount (should match calculated fee)
     * @return PaymentResponse with payment status and transaction ID
     */
    @PostMapping("/parking/payment/simulate")
    @Operation(
        summary = "Simulate payment",
        description = "Process simulated payment for parking session (for testing/demo purposes)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment processed successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
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

