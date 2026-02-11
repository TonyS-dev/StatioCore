package org.codeup.statiocore.service.strategy;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * Factory for fee calculation strategies (Strategy Pattern).
 * Maps spot types to pricing strategies: STANDARD ($10/hr), VIP ($15/hr).
 *
 * @author TonyS-dev
 */
@Component
public class FeeCalculatorFactory {

    private final Map<String, IFeeCalculationStrategy> strategies;

    public FeeCalculatorFactory(Optional<Map<String, IFeeCalculationStrategy>> strategies) {
        // Spring autowires all IFeeCalculationStrategy beans
        this.strategies = strategies.orElse(Map.of());
    }

    /**
     * Get pricing strategy by key (case-insensitive).
     * Keys: "STANDARD", "VIP", or bean names like "standardFeeStrategy".
     */
    public IFeeCalculationStrategy getStrategy(String key) {
        if (key == null) {
            return strategies.values().stream().findFirst().orElseThrow(() -> new IllegalStateException("No fee strategies available"));
        }
        String normalized = key.trim().toUpperCase();
        // try common names
        if (normalized.equals("VIP") && strategies.containsKey("vipFeeStrategy")) {
            return strategies.get("vipFeeStrategy");
        }
        if (normalized.equals("STANDARD") && strategies.containsKey("standardFeeStrategy")) {
            return strategies.get("standardFeeStrategy");
        }
        // try by bean name
        String beanKey = key.substring(0, 1).toLowerCase() + key.substring(1);
        if (strategies.containsKey(beanKey)) {
            return strategies.get(beanKey);
        }

        // fallback to first available
        return strategies.values().stream().findFirst().orElseThrow(() -> new IllegalStateException("No fee strategies available"));
    }
}

