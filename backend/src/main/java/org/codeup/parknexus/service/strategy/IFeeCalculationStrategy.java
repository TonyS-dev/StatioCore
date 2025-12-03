package org.codeup.parknexus.service.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

/**
 * Strategy interface for fee calculation.
 */
public interface IFeeCalculationStrategy {
    /**
     * Calculate the fee for a parking session given its duration.
     *
     * @param duration parking duration (non-null)
     * @return calculated fee as BigDecimal (scale 2)
     */
    BigDecimal calculateFee(Duration duration);

    /**
     * Optional: name of the strategy for logging or selection.
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Validates duration is non-null, positive and non-zero.
     */
    default boolean isValidDuration(Duration duration) {
        return duration != null && !duration.isNegative() && !duration.isZero();
    }

    /**
     * Returns zero fee with scale 2.
     */
    default BigDecimal zeroFee() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
