package com.shipmonk.testingday.rates.service;

import com.shipmonk.testingday.rates.model.CurrencyRate;
import com.shipmonk.testingday.rates.model.DailyExchangeRate;
import com.shipmonk.testingday.rates.model.ExchangeRateResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExchangeRateCalculator {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    public ExchangeRateResponse convertToDto(DailyExchangeRate original, String targetCurrency) {
        BigDecimal pivotRate = getPivotRate(original, targetCurrency);

        Map<String, BigDecimal> newRates = calculateNewRates(original, targetCurrency, pivotRate);

        return new ExchangeRateResponse(original.getDate(), targetCurrency, newRates);
    }

    public DailyExchangeRate convert(DailyExchangeRate original, String targetCurrency) {
        ExchangeRateResponse dto = convertToDto(original, targetCurrency);

        List<CurrencyRate> ratesList = dto.getRates().entrySet().stream()
            .map(entry -> new CurrencyRate(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        return new DailyExchangeRate(dto.getDate(), dto.getSource(), ratesList);
    }

    private BigDecimal getPivotRate(DailyExchangeRate original, String targetCurrency) {
        if (original.getSource().equalsIgnoreCase(targetCurrency)) {
            return BigDecimal.ONE;
        }
        return original.getRates().stream()
            .filter(r -> r.getCurrencyCode().equalsIgnoreCase(targetCurrency))
            .map(CurrencyRate::getExchangeRate)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Target currency " + targetCurrency + " not found in rates"));
    }

    private Map<String, BigDecimal> calculateNewRates(DailyExchangeRate original, String targetCurrency, BigDecimal pivotRate) {
        Map<String, BigDecimal> newRates = new LinkedHashMap<>();

        if (!original.getSource().equalsIgnoreCase(targetCurrency)) {
            newRates.put(original.getSource(), BigDecimal.ONE.divide(pivotRate, MATH_CONTEXT));
        }

        for (CurrencyRate oldRate : original.getRates()) {
            if (oldRate.getCurrencyCode().equalsIgnoreCase(targetCurrency)) {
                continue;
            }
            newRates.put(oldRate.getCurrencyCode(),
                oldRate.getExchangeRate().divide(pivotRate, MATH_CONTEXT));
        }
        return newRates;
    }
}
