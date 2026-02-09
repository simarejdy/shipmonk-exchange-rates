package com.shipmonk.testingday.rates.client;

import com.shipmonk.testingday.config.FixerProperties;
import com.shipmonk.testingday.rates.exception.FixerApiException;
import com.shipmonk.testingday.rates.model.CurrencyRate;
import com.shipmonk.testingday.rates.model.DailyExchangeRate;
import com.shipmonk.testingday.rates.service.ExchangeRateCalculator;
import com.shipmonk.testingday.rates.service.ExchangeRateProvider;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FixerApiAdapter implements ExchangeRateProvider {

    private static final String FALLBACK_BASE_CURRENCY = "EUR";
    private static final String TARGET_CURRENCY = "USD";

    private final FixerClient fixerClient;
    private final ExchangeRateCalculator calculator;
    private final FixerProperties properties;

    public FixerApiAdapter(FixerClient fixerClient,
                           ExchangeRateCalculator calculator,
                           FixerProperties properties) {
        this.fixerClient = fixerClient;
        this.calculator = calculator;
        this.properties = properties;
    }

    @Override
    @CircuitBreaker(name = "fixer-api", fallbackMethod = "fallbackRates")
    public DailyExchangeRate fetchRates(LocalDate date) {
        String requestBase = properties.isAllowCustomCurrencyBase() ? TARGET_CURRENCY : FALLBACK_BASE_CURRENCY;

        log.info("Requesting rates for date: {} [Base: {}]", date, requestBase);

        try {
            FixerResponseDto response = fixerClient.getRates(date, requestBase, "USD,CZK,GBP");

            if (response == null || !response.success()) {
                log.error("Fixer.io Business Error: {}", response);
                throw new FixerApiException("Fixer API returned success=false. Check API Key or Plan constraints.");
            }

            List<CurrencyRate> currencyRates = response.rates().entrySet().stream()
                .map(entry -> new CurrencyRate(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

            DailyExchangeRate entity = new DailyExchangeRate(
                response.date(),
                response.base(),
                currencyRates
            );

            if (!entity.getSource().equalsIgnoreCase(TARGET_CURRENCY)) {
                log.info("Normalizing rates from {} to target {}", entity.getSource(), TARGET_CURRENCY);
                return calculator.convert(entity, TARGET_CURRENCY);
            }

            return entity;

        } catch (FeignException e) {
            log.error("Network Failure: {}", e.getMessage());
            throw new FixerApiException("External service unavailable", e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching rates", e);
            throw new FixerApiException("Unexpected error processing exchange rates", e);
        }
    }

    @SuppressWarnings("unused")
    public DailyExchangeRate fallbackRates(LocalDate date, Throwable t) {
        log.error("Circuit Breaker Open: Fixer API unavailable for date {}. Reason: {}", date, t.getMessage());
        throw new FixerApiException("Service temporarily unavailable. Please try again later.", t);
    }
}
