package org.codeup.parknexus.repository;

import org.codeup.parknexus.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IPaymentRepository extends JpaRepository<Payment, UUID> {
}

