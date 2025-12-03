package org.codeup.parknexus.service.strategy;

import java.math.BigDecimal;
import java.time.Duration;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

/**
 * Standard fee strategy: $10 per hour (prorated by minutes).
 */
@Component
public class StandardFeeStrategy implements IFeeCalculationStrategy {
    private static final BigDecimal RATE_PER_HOUR = BigDecimal.valueOf(10);

    @Override
    public BigDecimal calculateFee(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return BigDecimal.ZERO.setScale(2);
        }
        // Calculate hours as fractional (minutes / 60)
        long minutes = duration.toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return hours.multiply(RATE_PER_HOUR).setScale(2, RoundingMode.HALF_UP);
    }
}

