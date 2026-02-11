package org.codeup.statiocore.repository;

import org.codeup.statiocore.domain.Payment;
import org.codeup.statiocore.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface IPaymentRepository extends JpaRepository<Payment, UUID> {
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") PaymentStatus status);
}

