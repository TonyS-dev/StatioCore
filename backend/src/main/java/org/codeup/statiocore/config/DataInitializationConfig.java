package org.codeup.statiocore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeup.statiocore.domain.*;
import org.codeup.statiocore.domain.enums.Role;
import org.codeup.statiocore.domain.enums.ReservationStatus;
import org.codeup.statiocore.domain.enums.SessionStatus;
import org.codeup.statiocore.domain.enums.PaymentMethod;
import org.codeup.statiocore.domain.enums.PaymentStatus;
import org.codeup.statiocore.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DataInitializationConfig - Seeds initial test data on application startup
 * This ensures users are created with properly hashed passwords through the
 * service layer
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataInitializationConfig {

        private final IUserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final IParkingSpotRepository parkingSpotRepository;
        private final IParkingSessionRepository parkingSessionRepository;
        private final IReservationRepository reservationRepository;
        private final IActivityLogRepository activityLogRepository;
        private final IPaymentRepository paymentRepository;

        @Bean
        public CommandLineRunner initializeTestData() {
                return args -> {
                        log.info("Starting data initialization...");

                        // Check if admin user already exists
                        if (userRepository.existsByEmail("admin@statiocore.com")) {
                                log.info("Test data already exists (admin user found), skipping initialization");
                                return;
                        }

                        log.info("Creating test users with properly hashed passwords...");

                        // Create Admin User 1 - Let JPA generate ID
                        User admin1 = User.builder()
                                        .email("admin@statiocore.com")
                                        .passwordHash(passwordEncoder.encode("password123"))
                                        .fullName("Admin User")
                                        .role(Role.ADMIN)
                                        .isActive(true)
                                        .build();

                        // Create Regular User 1
                        User user1 = User.builder()
                                        .email("john.doe@example.com")
                                        .passwordHash(passwordEncoder.encode("password123"))
                                        .fullName("John Doe")
                                        .role(Role.USER)
                                        .isActive(true)
                                        .build();

                        // Create Regular User 2
                        User user2 = User.builder()
                                        .email("jane.smith@example.com")
                                        .passwordHash(passwordEncoder.encode("password123"))
                                        .fullName("Jane Smith")
                                        .role(Role.USER)
                                        .isActive(true)
                                        .build();

                        // Create Regular User 3
                        User user3 = User.builder()
                                        .email("bob.wilson@example.com")
                                        .passwordHash(passwordEncoder.encode("password123"))
                                        .fullName("Bob Wilson")
                                        .role(Role.USER)
                                        .isActive(true)
                                        .build();

                        // Create Inactive User
                        User inactiveUser = User.builder()
                                        .email("alice.johnson@example.com")
                                        .passwordHash(passwordEncoder.encode("password123"))
                                        .fullName("Alice Johnson")
                                        .role(Role.USER)
                                        .isActive(false)
                                        .build();

                        // Create Admin User 2
                        User admin2 = User.builder()
                                        .email("admin2@statiocore.com")
                                        .passwordHash(passwordEncoder.encode("password123"))
                                        .fullName("Admin User 2")
                                        .role(Role.ADMIN)
                                        .isActive(true)
                                        .build();

                        // Save all users and capture the saved instances (with generated IDs)
                        var savedUsers = userRepository
                                        .saveAll(java.util.List.of(admin1, user1, user2, user3, inactiveUser, admin2));
                        var savedUserList = new java.util.ArrayList<>(savedUsers);

                        // Reassign to use saved instances with generated IDs
                        admin1 = savedUserList.get(0);
                        user1 = savedUserList.get(1);
                        user2 = savedUserList.get(2);
                        user3 = savedUserList.get(3);
                        inactiveUser = savedUserList.get(4);
                        admin2 = savedUserList.get(5);

                        log.info("Test users created successfully! ({} users saved)", savedUsers.size());
                        log.info("Test credentials - Email: admin@statiocore.com, Password: password123");

                        // ==========================================
                        // Seed Parking Sessions (10 sessions - mixed statuses)
                        // ==========================================
                        log.info("Creating parking sessions...");

                        ParkingSpot spot1 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440001")).orElse(null);
                        ParkingSpot spot3 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440003")).orElse(null);
                        ParkingSpot spot5 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440005")).orElse(null);
                        ParkingSpot spot6 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440006")).orElse(null);
                        ParkingSpot spot8 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440008")).orElse(null);
                        ParkingSpot spot9 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440009")).orElse(null);
                        ParkingSpot spot11 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440011")).orElse(null);
                        ParkingSpot spot12 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440012")).orElse(null);
                        ParkingSpot spot13 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440013")).orElse(null);
                        ParkingSpot spot14 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440014")).orElse(null);

                        if (spot1 != null && spot3 != null) {
                                // Active sessions - let JPA generate IDs
                                ParkingSession session1 = ParkingSession.builder()
                                                .user(user1)
                                                .spot(spot1)
                                                .checkInTime(OffsetDateTime.now().minusHours(2))
                                                .checkOutTime(null)
                                                .amountDue(null)
                                                .status(SessionStatus.ACTIVE)
                                                .vehicleNumber("ABC-1234")
                                                .build();

                                ParkingSession session2 = ParkingSession.builder()
                                                .user(user2)
                                                .spot(spot11)
                                                .checkInTime(OffsetDateTime.now().minusHours(1))
                                                .checkOutTime(null)
                                                .amountDue(null)
                                                .status(SessionStatus.ACTIVE)
                                                .build();

                                // Completed sessions
                                ParkingSession session3 = ParkingSession.builder()
                                                .user(user1)
                                                .spot(spot3)
                                                .checkInTime(OffsetDateTime.now().minusDays(1))
                                                .checkOutTime(OffsetDateTime.now().minusDays(1).plusHours(4))
                                                .amountDue(new BigDecimal("40.00"))
                                                .status(SessionStatus.COMPLETED)
                                                .vehicleNumber("ABC-1234")
                                                .build();

                                ParkingSession session4 = ParkingSession.builder()
                                                .user(user2)
                                                .spot(spot5)
                                                .checkInTime(OffsetDateTime.now().minusDays(2))
                                                .checkOutTime(OffsetDateTime.now().minusDays(2).plusHours(5))
                                                .amountDue(new BigDecimal("50.00"))
                                                .status(SessionStatus.COMPLETED)
                                                .build();

                                ParkingSession session5 = ParkingSession.builder()
                                                .user(user3)
                                                .spot(spot6)
                                                .checkInTime(OffsetDateTime.now().minusDays(3))
                                                .checkOutTime(OffsetDateTime.now().minusDays(3).plusHours(3))
                                                .amountDue(new BigDecimal("30.00"))
                                                .status(SessionStatus.COMPLETED)
                                                .build();

                                ParkingSession session6 = ParkingSession.builder()
                                                .user(user1)
                                                .spot(spot8)
                                                .checkInTime(OffsetDateTime.now().minusDays(4))
                                                .checkOutTime(OffsetDateTime.now().minusDays(4).plusHours(6))
                                                .amountDue(new BigDecimal("60.00"))
                                                .status(SessionStatus.COMPLETED)
                                                .vehicleNumber("ABC-1234")
                                                .build();

                                ParkingSession session7 = ParkingSession.builder()
                                                .user(user2)
                                                .spot(spot9)
                                                .checkInTime(OffsetDateTime.now().minusDays(5))
                                                .checkOutTime(OffsetDateTime.now().minusDays(5).plusHours(4))
                                                .amountDue(new BigDecimal("40.00"))
                                                .status(SessionStatus.COMPLETED)
                                                .build();

                                ParkingSession session8 = ParkingSession.builder()
                                                .user(user3)
                                                .spot(spot12)
                                                .checkInTime(OffsetDateTime.now().minusDays(6))
                                                .checkOutTime(OffsetDateTime.now().minusDays(6).plusHours(7))
                                                .amountDue(new BigDecimal("70.00"))
                                                .status(SessionStatus.COMPLETED)
                                                .build();

                                ParkingSession session9 = ParkingSession.builder()
                                                .user(user1)
                                                .spot(spot13)
                                                .checkInTime(OffsetDateTime.now().minusDays(7))
                                                .checkOutTime(OffsetDateTime.now().minusDays(7).plusHours(5))
                                                .amountDue(new BigDecimal("50.00"))
                                                .status(SessionStatus.COMPLETED)
                                                .vehicleNumber("ABC-1234")
                                                .build();

                                ParkingSession session10 = ParkingSession.builder()
                                                .user(user2)
                                                .spot(spot14)
                                                .checkInTime(OffsetDateTime.now().minusDays(8))
                                                .checkOutTime(OffsetDateTime.now().minusDays(8).plusHours(8))
                                                .amountDue(new BigDecimal("80.00"))
                                                .status(SessionStatus.COMPLETED)
                                                .build();

                                var savedSessions = parkingSessionRepository.saveAll(java.util.List.of(
                                                session1, session2, session3, session4, session5,
                                                session6, session7, session8, session9, session10));
                                log.info("Parking sessions created successfully! ({} sessions saved)",
                                                savedSessions.size());

                                // ==========================================
                                // Seed Payments (payment records for completed sessions)
                                // ==========================================
                                log.info("Creating payment records...");

                                var savedSessionsList = new java.util.ArrayList<>(savedSessions);

                                // Create payments for completed sessions (sessions 3-10)
                                java.util.List<Payment> payments = new java.util.ArrayList<>();

                                for (int i = 2; i < savedSessionsList.size(); i++) {
                                        ParkingSession session = savedSessionsList.get(i);
                                        if (session.getStatus() == SessionStatus.COMPLETED
                                                        && session.getAmountDue() != null) {
                                                Payment payment = Payment.builder()
                                                                .session(session)
                                                                .amount(session.getAmountDue())
                                                                .method(PaymentMethod.CREDIT_CARD)
                                                                .currency("USD")
                                                                .status(PaymentStatus.SUCCESS)
                                                                .transactionReference("TXN-" + session.getId()
                                                                                .toString().substring(0, 8)
                                                                                .toUpperCase())
                                                                .build();
                                                payments.add(payment);
                                        }
                                }

                                if (!payments.isEmpty()) {
                                        var savedPayments = paymentRepository.saveAll(payments);
                                        log.info("Payment records created successfully! ({} payments saved)",
                                                        savedPayments.size());
                                }
                        }

                        // ==========================================
                        // Seed Reservations (8 reservations - mixed statuses)
                        // ==========================================
                        log.info("Creating reservations...");

                        ParkingSpot spot15 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440015")).orElse(null);
                        ParkingSpot spot16 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440016")).orElse(null);
                        ParkingSpot spot17 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440017")).orElse(null);
                        ParkingSpot spot18 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440018")).orElse(null);
                        ParkingSpot spot19 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440019")).orElse(null);
                        ParkingSpot spot20 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440020")).orElse(null);
                        ParkingSpot spot21 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440021")).orElse(null);
                        ParkingSpot spot22 = parkingSpotRepository
                                        .findById(UUID.fromString("850e8400-e29b-41d4-a716-446655440022")).orElse(null);

                        if (spot15 != null && spot16 != null) {
                                // Active reservations
                                Reservation res1 = Reservation.builder()
                                                .user(user1)
                                                .spot(spot15)
                                                .startTime(OffsetDateTime.now().plusHours(1))
                                                .endTime(OffsetDateTime.now().plusHours(5))
                                                .status(ReservationStatus.ACTIVE)
                                                .build();

                                Reservation res2 = Reservation.builder()
                                                .user(user2)
                                                .spot(spot16)
                                                .startTime(OffsetDateTime.now().plusHours(2))
                                                .endTime(OffsetDateTime.now().plusHours(6))
                                                .status(ReservationStatus.ACTIVE)
                                                .build();

                                Reservation res3 = Reservation.builder()
                                                .user(user3)
                                                .spot(spot17)
                                                .startTime(OffsetDateTime.now().plusHours(3))
                                                .endTime(OffsetDateTime.now().plusHours(7))
                                                .status(ReservationStatus.ACTIVE)
                                                .build();

                                // Completed reservations
                                Reservation res4 = Reservation.builder()
                                                .user(user1)
                                                .spot(spot18)
                                                .startTime(OffsetDateTime.now().minusDays(2))
                                                .endTime(OffsetDateTime.now().minusDays(2).plusHours(4))
                                                .status(ReservationStatus.COMPLETED)
                                                .build();

                                Reservation res5 = Reservation.builder()
                                                .user(user2)
                                                .spot(spot19)
                                                .startTime(OffsetDateTime.now().minusDays(3))
                                                .endTime(OffsetDateTime.now().minusDays(3).plusHours(5))
                                                .status(ReservationStatus.COMPLETED)
                                                .build();

                                // Cancelled reservations
                                Reservation res6 = Reservation.builder()
                                                .user(user3)
                                                .spot(spot20)
                                                .startTime(OffsetDateTime.now().minusDays(1))
                                                .endTime(OffsetDateTime.now().minusDays(1).plusHours(3))
                                                .status(ReservationStatus.CANCELLED)
                                                .build();

                                Reservation res7 = Reservation.builder()
                                                .user(user1)
                                                .spot(spot21)
                                                .startTime(OffsetDateTime.now().minusDays(5))
                                                .endTime(OffsetDateTime.now().minusDays(5).plusHours(6))
                                                .status(ReservationStatus.CANCELLED)
                                                .build();

                                Reservation res8 = Reservation.builder()
                                                .user(user2)
                                                .spot(spot22)
                                                .startTime(OffsetDateTime.now().minusDays(10))
                                                .endTime(OffsetDateTime.now().minusDays(10).plusHours(4))
                                                .status(ReservationStatus.COMPLETED)
                                                .build();

                                var savedReservations = reservationRepository.saveAll(java.util.List.of(
                                                res1, res2, res3, res4, res5, res6, res7, res8));
                                log.info("Reservations created successfully! ({} reservations saved)",
                                                savedReservations.size());
                        }

                        // ==========================================
                        // Seed Activity Logs (various actions)
                        // ==========================================
                        log.info("Creating activity logs...");

                        ActivityLog actLog1 = ActivityLog.builder()
                                        .user(user3)
                                        .action("PAYMENT_PROCESSED")
                                        .details("Payment processed successfully")
                                        .build();

                        ActivityLog actLog2 = ActivityLog.builder()
                                        .user(user1)
                                        .action("RESERVATION_CREATED")
                                        .details("User created a new reservation")
                                        .build();

                        ActivityLog actLog3 = ActivityLog.builder()
                                        .user(user2)
                                        .action("SESSION_STARTED")
                                        .details("User checked in to parking session")
                                        .build();

                        ActivityLog actLog4 = ActivityLog.builder()
                                        .user(admin2)
                                        .action("USER_ROLE_UPDATED")
                                        .details("Admin updated user role for " + user3.getId())
                                        .build();

                        var savedLogs = activityLogRepository
                                        .saveAll(java.util.List.of(actLog1, actLog2, actLog3, actLog4));
                        log.info("Activity logs created successfully! ({} logs saved)", savedLogs.size());
                        log.info("Data initialization completed!");

                        log.info("Data initialization completed!");
                };
        }
}
