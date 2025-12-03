package org.codeup.parknexus.service.strategy;

import java.math.BigDecimal;
import java.time.Duration;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

/**
 * VIP fee strategy: $20 per hour (prorated).
 */
@Component
public class VipFeeStrategy implements IFeeCalculationStrategy {
    private static final BigDecimal RATE_PER_HOUR = BigDecimal.valueOf(20);

    @Override
    public BigDecimal calculateFee(Duration duration) {
        if (!isValidDuration(duration)) {
            return zeroFee();
        }
        long minutes = duration.toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return hours.multiply(RATE_PER_HOUR).setScale(2, RoundingMode.HALF_UP);
    }
}
