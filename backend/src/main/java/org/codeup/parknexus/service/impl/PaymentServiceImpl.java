package org.codeup.parknexus.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.Payment;
import org.codeup.parknexus.domain.ParkingSession;
import org.codeup.parknexus.domain.enums.PaymentMethod;
import org.codeup.parknexus.domain.enums.PaymentStatus;
import org.codeup.parknexus.exception.ResourceNotFoundException;
import org.codeup.parknexus.repository.IPaymentRepository;
import org.codeup.parknexus.service.IPaymentService;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements IPaymentService {
    private final IPaymentRepository paymentRepository;

    @Override
    public void createPayment(ParkingSession session, BigDecimal amountDue, PaymentMethod method, String currency) {
        Payment payment = Payment.builder()
                .session(session)
                .amount(amountDue)
                .method(method)
                .currency(currency)
                .status(PaymentStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();
        paymentRepository.save(payment);
    }

    @Override
    public Payment markPaid(UUID paymentId, String transactionReference) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionReference(transactionReference);
        payment.setUpdatedAt(OffsetDateTime.now());
        return paymentRepository.save(payment);
    }

    @Override
    public Payment markFailed(UUID paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.FAILED);
        payment.setTransactionReference(reason);
        payment.setUpdatedAt(OffsetDateTime.now());
        return paymentRepository.save(payment);
    }
}
