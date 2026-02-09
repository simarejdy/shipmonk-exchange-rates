package com.shipmonk.testingday.rates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FixerApiAdapter implements ExchangeRateProvider {

    private final FixerClient fixerClient;
    private final ExchangeRateCalculator calculator;
    private final String plan;

    public FixerApiAdapter(
        FixerClient fixerClient,
        ExchangeRateCalculator calculator,
        @Value("${shipmonk.fixer.plan}") String plan) {
        this.fixerClient = fixerClient;
        this.calculator = calculator;
        this.plan = plan;
    }

    @Override
    public DailyExchangeRate fetchRates(LocalDate date) {
        // 1. SCALABILITY: Decide what to ask for based on the Plan
        // Paid Plan -> Ask for USD directly (Save CPU)
        // Free Plan -> Must ask for EUR (API Constraint)
        String requestBase = "PAID".equalsIgnoreCase(plan) ? "USD" : "EUR";

        // 2. Fetch
        FixerResponseDto response = fixerClient.getRates(date, requestBase, "USD,CZK,EUR,GBP");

        if (response == null || !response.success()) {
            throw new IllegalStateException("Fixer API failed");
        }

        // 3. Map DTO -> Entity (List)
        List<CurrencyRate> currencyRates = response.rates().entrySet().stream()
            .map(entry -> new CurrencyRate(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        DailyExchangeRate rate = new DailyExchangeRate(
            response.date(),
            response.base(),
            currencyRates
        );

        // 4. SCALABILITY: Only run the math if the API didn't give us what we wanted.
        // If we asked for USD and got USD, this line does nothing (Calculater is smart).
        // If we asked for EUR (Free), this converts it.
        return calculator.convert(rate, "USD");
    }
}
