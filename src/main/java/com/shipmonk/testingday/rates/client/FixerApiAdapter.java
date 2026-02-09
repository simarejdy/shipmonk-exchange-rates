package com.shipmonk.testingday.rates.client;

import com.shipmonk.testingday.config.FixerProperties;
import com.shipmonk.testingday.rates.exception.FixerApiException;
import com.shipmonk.testingday.rates.model.CurrencyRate;
import com.shipmonk.testingday.rates.model.DailyExchangeRate;
import com.shipmonk.testingday.rates.service.ExchangeRateCalculator;
import com.shipmonk.testingday.rates.service.ExchangeRateProvider;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FixerApiAdapter implements ExchangeRateProvider {

    private static final String FALLBACK_BASE_CURRENCY = "EUR";

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
    public DailyExchangeRate fetchRates(LocalDate date) {
        String targetCurrency = properties.getBaseCurrency();
        String requestBase = properties.getCapabilities().isAllowCustomCurrencyBase()
            ? targetCurrency
            : FALLBACK_BASE_CURRENCY;

        String targetCurrencies = String.join(",", properties.getTargetCurrencies());

        log.info("Requesting rates for date: {} [Base: {}]", date, requestBase);

        try {
            FixerResponseDto response = fixerClient.getRates(date, requestBase, targetCurrencies);

            if (response == null) {
                log.error("Fixer API returned NULL response body for date: {}", date);
                throw new FixerApiException("Fixer API returned empty response.");
            }

            if (!response.success()) {
                log.error("Fixer.io Business Error: {}", response);
                String errorInfo = (response.error() != null) ? response.error().info() : "Unknown Error";
                throw new FixerApiException("Fixer API failed: " + errorInfo);
            }

            if (response.rates() == null || response.rates().isEmpty()) {
                log.error("Fixer.io returned success=true but NO rates. Response: {}", response);
                throw new FixerApiException("Fixer API returned empty rates list.");
            }

            // 6. Map DTO to Entity
            List<CurrencyRate> currencyRates = response.rates().entrySet().stream()
                .map(entry -> new CurrencyRate(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

            DailyExchangeRate entity = new DailyExchangeRate(
                response.date(),
                response.base(),
                currencyRates
            );

            if (!entity.getSource().equalsIgnoreCase(targetCurrency)) {
                log.info("Normalizing rates from {} to target {}", entity.getSource(), targetCurrency);
                return calculator.convert(entity, targetCurrency);
            }

            return entity;

        } catch (FeignException e) {
            log.error("Network Failure: Status {}, Body: {}", e.status(), e.contentUTF8(), e);
            throw new FixerApiException("External service unavailable", e);
        } catch (Exception e) {
            log.error("Unexpected error while processing rates", e);
            throw new FixerApiException("Unexpected error processing exchange rates", e);
        }
    }

}
