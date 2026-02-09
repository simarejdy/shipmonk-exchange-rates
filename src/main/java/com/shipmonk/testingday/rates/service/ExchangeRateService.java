package com.shipmonk.testingday.rates.service;

import com.shipmonk.testingday.rates.model.DailyExchangeRate;
import com.shipmonk.testingday.rates.model.ExchangeRateResponse; // Import the DTO
import com.shipmonk.testingday.rates.repository.ExchangeRateRepository;
import com.shipmonk.testingday.config.FixerProperties;
import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExchangeRateService {

    private final ExchangeRateRepository repository;
    private final ExchangeRateProvider provider;
    private final ExchangeRateCalculator calculator;
    private final FixerProperties properties;

    public ExchangeRateService(ExchangeRateRepository repository,
                               ExchangeRateProvider provider,
                               ExchangeRateCalculator calculator,
                               FixerProperties properties) {
        this.repository = repository;
        this.provider = provider;
        this.calculator = calculator;
        this.properties = properties;
    }

    @Transactional
    public ExchangeRateResponse getRatesForDate(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the future: " + date);
        }

        String baseCurrency = properties.getBaseCurrency();
        Optional<DailyExchangeRate> cachedRate = repository.findByDate(date, baseCurrency);

        if (cachedRate.isPresent()) {
            log.info("Cache : Found rates for date {} in database. Returning instantly.", date);

            return calculator.convertToDto(cachedRate.get(), baseCurrency);
        }

        return fetchAndCache(date);
    }

    private ExchangeRateResponse fetchAndCache(LocalDate date) {
        log.info("Cache : Rate not found for date {}. Fetching from provider...", date);
        String baseCurrency = properties.getBaseCurrency();

        try {
            DailyExchangeRate fetchedRate = provider.fetchRates(date);

            if (log.isInfoEnabled()) {
                String values = fetchedRate.getRates().stream()
                    .map(r -> r.getCurrencyCode() + ":" + r.getExchangeRate())
                    .collect(Collectors.joining(", "));

                log.info("Fetched and calculated for {}: Base={}, Values=[{}]",
                    date, fetchedRate.getSource(), values);
            }

            repository.persist(fetchedRate);
            repository.flush();
            log.info("Successfully persisted rates for date: {}", date);

            return calculator.convertToDto(fetchedRate, baseCurrency);

        } catch (DataIntegrityViolationException | EntityExistsException e) {
            log.warn("Race condition detected for date: {}. Reading from DB instead.", date);

            return repository.findByDate(date, baseCurrency)
                .map(rate -> calculator.convertToDto(rate, baseCurrency))
                .orElseThrow(() -> new IllegalStateException("Concurrency Error: Rate saved but not found.", e));
        }
    }
}
