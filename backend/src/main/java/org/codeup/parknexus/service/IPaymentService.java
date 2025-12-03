package org.codeup.parknexus.service;

import org.codeup.parknexus.domain.Payment;
import org.codeup.parknexus.domain.ParkingSession;
import org.codeup.parknexus.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public interface IPaymentService {
    void createPayment(ParkingSession session, BigDecimal amountDue, PaymentMethod method, String currency);
    Payment markPaid(UUID paymentId, String transactionReference);
    Payment markFailed(UUID paymentId, String reason);
}
