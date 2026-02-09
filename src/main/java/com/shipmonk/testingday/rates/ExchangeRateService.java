package com.shipmonk.testingday.rates;

import jakarta.persistence.EntityExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private final ExchangeRateRepository repository;
    private final ExchangeRateProvider provider;
    private final ExchangeRateCalculator calculator; // <--- NEW DEPENDENCY

    public ExchangeRateService(
        ExchangeRateRepository repository,
        ExchangeRateProvider provider,
        ExchangeRateCalculator calculator) {
        this.repository = repository;
        this.provider = provider;
        this.calculator = calculator;
    }

    @Transactional
    public DailyExchangeRate getRatesForDate(LocalDate date) {
        return repository.findByDate(date)
            // Use the Calculator "Brain" instead of the Entity method
            .map(rate -> calculator.convert(rate, "USD"))
            .orElseGet(() -> fetchAndCache(date));
    }

    private DailyExchangeRate fetchAndCache(LocalDate date) {
        log.info("Rate not found in cache for date: {}. Fetching from provider...", date);

        try {
            // 1. Fetch (Adapter returns USD or EUR depending on logic, but usually USD now)
            DailyExchangeRate newRate = provider.fetchRates(date);

            // 2. Save to DB
            repository.persist(newRate);

            log.info("Successfully cached rates for date: {}", date);

            // 3. Ensure we return USD (Calculator handles optimization if it's already USD)
            return calculator.convert(newRate, "USD");

        } catch (DataIntegrityViolationException | EntityExistsException e) {
            log.warn("Race condition detected for date: {}. Reading from DB instead.", date);

            return repository.findByDate(date)
                .map(rate -> calculator.convert(rate, "USD"))
                .orElseThrow(() -> new IllegalStateException("Concurrency Error: Rate saved but not found.", e));
        }
    }
}
