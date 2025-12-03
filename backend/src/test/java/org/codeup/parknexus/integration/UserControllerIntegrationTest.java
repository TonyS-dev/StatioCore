package org.codeup.parknexus.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codeup.parknexus.domain.enums.PaymentMethod;
import org.codeup.parknexus.domain.enums.SpotType;
import org.codeup.parknexus.web.dto.user.CheckInRequest;
import org.codeup.parknexus.web.dto.user.ReservationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for User Controller
 *
 * Tests complete user workflows and endpoint integration:
 * - User authentication and authorization
 * - Parking spot search with filters
 * - Check-in/Check-out workflows
 * - Fee calculation
 * - Payment processing
 * - Reservation management
 *
 * Uses in-memory H2 database and MockMvc for HTTP testing.
 *
 * @author TonyS-dev
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Controller Integration Tests")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_EMAIL = "john.doe@example.com";
    private static final String USER_ID = "1c3cc01f-0216-43d0-98b8-970df0daaa98"; // From seed data
    private static final UUID DOWNTOWN_BUILDING_ID = UUID.fromString("650e8400-e29b-41d4-a716-446655440001");
    private static final UUID DOWNTOWN_FLOOR_1_ID = UUID.fromString("750e8400-e29b-41d4-a716-446655440001");
    private static final UUID SPOT_A1_ID = UUID.fromString("850e8400-e29b-41d4-a716-446655440001");

    @BeforeEach
    void setUp() {
        // Test database is seeded with initial data via Flyway migrations
    }

    // =====================================================================
    // 1. DASHBOARD TESTS
    // =====================================================================

    @Test
    @DisplayName("GET /api/user/dashboard - Authenticated user retrieves dashboard")
    @WithMockUser(username = USER_EMAIL, roles = "USER")
    void testGetUserDashboard() throws Exception {
        mockMvc.perform(get("/api/user/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalSpots").isNumber())
            .andExpect(jsonPath("$.availableSpots").isNumber())
            .andExpect(jsonPath("$.occupiedSpots").isNumber())
            .andExpect(jsonPath("$.occupancyPercentage").isNumber())
            .andExpect(jsonPath("$.activeReservations").isNumber())
            .andExpect(jsonPath("$.activeSessions").isNumber())
            .andExpect(jsonPath("$.recentActivity").isArray());
    }

    @Test
    @DisplayName("GET /api/user/dashboard - Unauthenticated request returns 401")
    void testGetDashboardUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/dashboard"))
            .andExpect(status().isUnauthorized());
    }

    // =====================================================================
    // 2. BUILDINGS & FILTERING TESTS
    // =====================================================================

    @Test
    @DisplayName("GET /api/buildings - Public endpoint returns all buildings")
    void testGetAllBuildings() throws Exception {
        mockMvc.perform(get("/api/buildings")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
            .andExpect(jsonPath("$[*].name", hasItems(
                "Downtown Tower", "Airport Plaza", "Mall Central", "Business Park", "Hospital Garage")))
            .andExpect(jsonPath("$[0].totalFloors").isNumber())
            .andExpect(jsonPath("$[0].totalSpots").isNumber());
    }

    @Test
    @DisplayName("GET /api/buildings - No authentication required for buildings")
    void testGetBuildingsNoAuth() throws Exception {
        // Should work without authentication
        mockMvc.perform(get("/api/buildings"))
            .andExpect(status().isOk());
    }

    // =====================================================================
    // 3. PARKING SPOT SEARCH TESTS
    // =====================================================================

    @Test
    @DisplayName("GET /api/spots/available - Retrieve all available parking spots")
    @WithMockUser(username = USER_EMAIL, roles = "USER")
    void testGetAvailableSpots() throws Exception {
        mockMvc.perform(get("/api/spots/available")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", isA(java.util.List.class)))
            .andExpect(jsonPath("$[0].spotNumber").exists())
            .andExpect(jsonPath("$[0].type").exists())
            .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("GET /api/spots/available - Filter by building")
    @WithMockUser(username = USER_EMAIL, roles = "USER")
    void testGetAvailableSpotsByBuilding() throws Exception {
        mockMvc.perform(get("/api/spots/available")
                .param("buildingId", DOWNTOWN_BUILDING_ID.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].buildingName", everyItem(is("Downtown Tower"))));
    }

    @Test
    @DisplayName("GET /api/spots/available - Filter by floor")
    @WithMockUser(username = USER_EMAIL, roles = "USER")
    void testGetAvailableSpotsByFloor() throws Exception {
        mockMvc.perform(get("/api/spots/available")
                .param("floorId", DOWNTOWN_FLOOR_1_ID.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].floorName", everyItem(is("Floor 1"))));
    }

    @Test
    @DisplayName("GET /api/spots/available - Filter by type: VIP")
    @WithMockUser(username = USER_EMAIL, roles = "USER")
    void testGetVipSpots() throws Exception {
        mockMvc.perform(get("/api/spots/available")
                .param("type", "VIP")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].type", everyItem(is("VIP"))));
    }

    @Test
    @DisplayName("GET /api/spots/available - Requires authentication")
    void testGetAvailableSpotsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/spots/available"))
            .andExpect(status().isUnauthorized());
    }

    // =====================================================================
    // 4. CHECK-IN/CHECK-OUT WORKFLOW TESTS
    // =====================================================================

    @Test
    @DisplayName("POST /api/parking/check-in - User checks in to available spot")
    @WithMockUser(username = USER_EMAIL, roles = "USER", value = USER_ID)
    void testCheckIn() throws Exception {
        CheckInRequest request = CheckInRequest.builder()
            .spotId(SPOT_A1_ID)
            .vehicleNumber("ABC-123")
            .build();

        mockMvc.perform(post("/api/parking/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").exists())
            .andExpect(jsonPath("$.spotNumber").value("A1"))
            .andExpect(jsonPath("$.vehicleNumber").value("ABC-123"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.message").value("Checked in successfully"));
    }

    @Test
    @DisplayName("POST /api/parking/check-in - Cannot check in to unavailable spot")
    @WithMockUser(username = USER_EMAIL, roles = "USER", value = USER_ID)
    void testCheckInToUnavailableSpot() throws Exception {
        // First check-in
        CheckInRequest request = CheckInRequest.builder()
            .spotId(SPOT_A1_ID)
            .vehicleNumber("ABC-123")
            .build();

        mockMvc.perform(post("/api/parking/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // Second check-in to same spot should fail
        mockMvc.perform(post("/api/parking/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/parking/calculate-fee - Calculate fee for active session")
    @WithMockUser(username = USER_EMAIL, roles = "USER", value = USER_ID)
    void testCalculateFee() throws Exception {
        // First check-in
        CheckInRequest checkInRequest = CheckInRequest.builder()
            .spotId(SPOT_A1_ID)
            .vehicleNumber("ABC-123")
            .build();

        MvcResult checkInResult = mockMvc.perform(post("/api/parking/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkInRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String checkInResponse = checkInResult.getResponse().getContentAsString();
        UUID sessionId = UUID.fromString(objectMapper.readTree(checkInResponse).get("sessionId").asText());

        // Sleep briefly to allow time to accumulate
        Thread.sleep(2000);

        // Calculate fee
        mockMvc.perform(post("/api/parking/calculate-fee")
                .param("sessionId", sessionId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
            .andExpect(jsonPath("$.durationMinutes").isNumber())
            .andExpect(jsonPath("$.hourlyRate").isNumber())
            .andExpect(jsonPath("$.amountDue").isNumber())
            .andExpect(jsonPath("$.amountDue", greaterThanOrEqualTo(1.0)))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/parking/check-out - Check out and process payment")
    @WithMockUser(username = USER_EMAIL, roles = "USER", value = USER_ID)
    void testCheckOut() throws Exception {
        // Check-in
        CheckInRequest checkInRequest = CheckInRequest.builder()
            .spotId(SPOT_A1_ID)
            .vehicleNumber("XYZ-789")
            .build();

        MvcResult checkInResult = mockMvc.perform(post("/api/parking/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkInRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String checkInResponse = checkInResult.getResponse().getContentAsString();
        UUID sessionId = UUID.fromString(objectMapper.readTree(checkInResponse).get("sessionId").asText());

        // Check-out
        mockMvc.perform(post("/api/parking/check-out")
                .param("sessionId", sessionId.toString())
                .param("paymentMethod", "CREDIT_CARD")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
            .andExpect(jsonPath("$.amountDue").isNumber())
            .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
            .andExpect(jsonPath("$.transactionId").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/parking/check-out - Cannot check out twice")
    @WithMockUser(username = USER_EMAIL, roles = "USER", value = USER_ID)
    void testDoubleCheckOut() throws Exception {
        // Check-in
        CheckInRequest checkInRequest = CheckInRequest.builder()
            .spotId(SPOT_A1_ID)
            .vehicleNumber("DUP-001")
            .build();

        MvcResult checkInResult = mockMvc.perform(post("/api/parking/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkInRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String checkInResponse = checkInResult.getResponse().getContentAsString();
        UUID sessionId = UUID.fromString(objectMapper.readTree(checkInResponse).get("sessionId").asText());

        // First check-out
        mockMvc.perform(post("/api/parking/check-out")
                .param("sessionId", sessionId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Second check-out should fail
        mockMvc.perform(post("/api/parking/check-out")
                .param("sessionId", sessionId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    // =====================================================================
    // 5. SESSION RETRIEVAL TESTS
    // =====================================================================

    @Test
    @DisplayName("GET /api/parking/sessions/active - Get active sessions")
    @WithMockUser(username = USER_EMAIL, roles = "USER", value = USER_ID)
    void testGetActiveSessions() throws Exception {
        mockMvc.perform(get("/api/parking/sessions/active")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("GET /api/parking/sessions/my - Get all sessions history")
    @WithMockUser(username = USER_EMAIL, roles = "USER", value = USER_ID)
    void testGetUserSessions() throws Exception {
        mockMvc.perform(get("/api/parking/sessions/my")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    // =====================================================================
    // 6. RESERVATION TESTS
    // =====================================================================

    @Test
    @DisplayName("POST /api/reservations - Create parking reservation")
    @WithMockUser(username = USER_EMAIL, roles = "USER", value = USER_ID)
    void testCreateReservation() throws Exception {
        ReservationRequest request = ReservationRequest.builder()
            .spotId(SPOT_A1_ID)
            .startTime(OffsetDateTime.now().plusHours(1))
            .durationMinutes(120)
            .build();

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.spotNumber").value("A1"))
            .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("GET /api/reservations - Get user's reservations")
    @WithMockUser(username = USER_EMAIL, roles = "USER", value = USER_ID)
    void testGetUserReservations() throws Exception {
        mockMvc.perform(get("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("DELETE /api/reservations/{id} - Cancel reservation")
    @WithMockUser(username = USER_EMAIL, roles = "USER", value = USER_ID)
    void testCancelReservation() throws Exception {
        // Create reservation
        ReservationRequest request = ReservationRequest.builder()
            .spotId(SPOT_A1_ID)
            .startTime(OffsetDateTime.now().plusHours(2))
            .durationMinutes(60)
            .build();

        MvcResult result = mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        UUID reservationId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        // Cancel reservation
        mockMvc.perform(delete("/api/reservations/" + reservationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    // =====================================================================
    // 7. PAYMENT SIMULATION TESTS
    // =====================================================================

    @Test
    @DisplayName("POST /api/parking/payment/simulate - Simulate payment")
    @WithMockUser(username = USER_EMAIL, roles = "USER", value = USER_ID)
    void testSimulatePayment() throws Exception {
        // Check-in
        CheckInRequest checkInRequest = CheckInRequest.builder()
            .spotId(SPOT_A1_ID)
            .vehicleNumber("PAY-001")
            .build();

        MvcResult checkInResult = mockMvc.perform(post("/api/parking/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkInRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String checkInResponse = checkInResult.getResponse().getContentAsString();
        UUID sessionId = UUID.fromString(objectMapper.readTree(checkInResponse).get("sessionId").asText());

        // Simulate payment
        mockMvc.perform(post("/api/parking/payment/simulate")
                .param("sessionId", sessionId.toString())
                .param("amount", "5.00")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").exists())
            .andExpect(jsonPath("$.amount").value(5.0))
            .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}

