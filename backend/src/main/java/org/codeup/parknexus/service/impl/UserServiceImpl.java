package org.codeup.parknexus.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.ParkingSession;
import org.codeup.parknexus.domain.Reservation;
import org.codeup.parknexus.domain.Payment;
import org.codeup.parknexus.domain.enums.SessionStatus;
import org.codeup.parknexus.domain.enums.PaymentStatus;
import org.codeup.parknexus.domain.enums.ReservationStatus;
import org.codeup.parknexus.repository.IParkingSessionRepository;
import org.codeup.parknexus.repository.IReservationRepository;
import org.codeup.parknexus.repository.IPaymentRepository;
import org.codeup.parknexus.service.IUserService;
import org.codeup.parknexus.web.dto.user.DashboardResponse;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements IUserService {
    private final IParkingSessionRepository sessionRepository;
    private final IReservationRepository reservationRepository;
    private final IPaymentRepository paymentRepository;

    @Override
    public DashboardResponse getDashboard(UUID userId) {
        List<Reservation> reservations = reservationRepository.findByUserIdOrderByStartTimeDesc(userId);
        List<ParkingSession> sessions = sessionRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();

        long activeReservations = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE || r.getStatus() == ReservationStatus.PENDING)
                .count();
        long totalReservations = reservations.size();
        long activeSessions = sessions.stream()
                .filter(s -> s.getUser() != null && userId.equals(s.getUser().getId()) && s.getStatus() == SessionStatus.ACTIVE)
                .count();
        BigDecimal outstandingFees = payments.stream()
                .filter(p -> p.getSession() != null && p.getSession().getUser() != null && userId.equals(p.getSession().getUser().getId()))
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardResponse.builder()
                .activeReservations(activeReservations)
                .activeSessions(activeSessions)
                .totalReservations(totalReservations)
                .outstandingFees(outstandingFees)
                .build();
    }
}
