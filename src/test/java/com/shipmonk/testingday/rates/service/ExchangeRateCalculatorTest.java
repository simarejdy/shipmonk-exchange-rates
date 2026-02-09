package com.shipmonk.testingday.rates.service;

import com.shipmonk.testingday.rates.model.CurrencyRate;
import com.shipmonk.testingday.rates.model.DailyExchangeRate;
import com.shipmonk.testingday.rates.model.ExchangeRateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ExchangeRateCalculatorTest {

    private final ExchangeRateCalculator calculator = new ExchangeRateCalculator();

    @Test
    @DisplayName("Should convert EUR base rates to USD base correctly (Free Tier Scenario)")
    void shouldConvertEurToUsd() {
        // GIVEN:
        // EUR -> USD = 1.10
        // EUR -> CZK = 25.00
        List<CurrencyRate> rates = Arrays.asList(
            new CurrencyRate("USD", new BigDecimal("1.10")),
            new CurrencyRate("CZK", new BigDecimal("25.00"))
        );
        DailyExchangeRate eurRate = new DailyExchangeRate(LocalDate.now(), "EUR", rates);

        // WHEN:
        ExchangeRateResponse result = calculator.convertToDto(eurRate, "USD");

        // THEN:
        assertThat(result.getSource()).isEqualTo("USD");

        assertThat(result.getRates().get("EUR"))
            .isBetween(new BigDecimal("0.90"), new BigDecimal("0.91"));

        assertThat(result.getRates().get("CZK"))
            .isBetween(new BigDecimal("22.72"), new BigDecimal("22.73"));
    }

    @Test
    @DisplayName("Should return identity if target currency is already the base")
    void shouldReturnIdentity() {
        // GIVEN:
        List<CurrencyRate> rates = List.of(new CurrencyRate("CZK", new BigDecimal("22.5")));
        DailyExchangeRate usdRate = new DailyExchangeRate(LocalDate.now(), "USD", rates);

        // WHEN:
        ExchangeRateResponse result = calculator.convertToDto(usdRate, "USD");

        // THEN:
        assertThat(result.getSource()).isEqualTo("USD");
        assertThat(result.getRates().get("CZK")).isEqualTo(new BigDecimal("22.5"));
    }

    @Test
    @DisplayName("Should throw exception if source rates are empty")
    void shouldThrowOnEmptyRates() {
        // GIVEN: Empty rates list
        DailyExchangeRate emptyRate = new DailyExchangeRate(LocalDate.now(), "EUR", Collections.emptyList());

        // WHEN/THEN: We try to convert
        assertThatThrownBy(() -> calculator.convertToDto(emptyRate, "USD"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Source rates list is empty");
    }
}
