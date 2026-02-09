package com.shipmonk.testingday.rates;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExchangeRateCalculator {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    /**
     * Converts a DailyExchangeRate object to a new base currency.
     * Does NOT modify the original entity. Returns a new one.
     */
    public DailyExchangeRate convert(DailyExchangeRate original, String targetCurrency) {
        // 1. Optimization: If already in target currency, return original
        if (original.getSource().equalsIgnoreCase(targetCurrency)) {
            return original;
        }

        // 2. Find the "Pivot Rate" (e.g., EUR -> USD)
        BigDecimal pivotRate = original.getRates().stream()
            .filter(r -> r.getCurrencyCode().equalsIgnoreCase(targetCurrency))
            .map(CurrencyRate::getExchangeRate)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Target currency " + targetCurrency + " not found in rates"));

        // 3. Create new list for converted rates
        List<CurrencyRate> newRates = new ArrayList<>();

        // 4. Math Loop
        for (CurrencyRate oldRate : original.getRates()) {
            // Formula: NewValue = OldValue / PivotValue
            BigDecimal newValue = oldRate.getExchangeRate().divide(pivotRate, MATH_CONTEXT);
            newRates.add(new CurrencyRate(oldRate.getCurrencyCode(), newValue));
        }

        // 5. Add the OLD base as a new rate (Inverse)
        // Example: If base was EUR, now 1 USD = (1 / Pivot) EUR
        BigDecimal oldBaseValue = BigDecimal.ONE.divide(pivotRate, MATH_CONTEXT);
        newRates.add(new CurrencyRate(original.getSource(), oldBaseValue));

        // 6. Return new decoupled object (not an Entity, just a DTO/Model really)
        // We use a protected constructor or setter here, typically.
        // For this task, we can use the constructor we already have.
        // We create a temporary map to feed the constructor or add a new constructor.

        // Simpler: Just instantiate and set fields if possible, or use a specific constructor.
        // Let's assume we add a helper constructor to DailyExchangeRate for this.
        return new DailyExchangeRate(original.getDate(), targetCurrency, newRates);
    }
}
