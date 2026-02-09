package com.shipmonk.testingday.rates.service;

import com.shipmonk.testingday.rates.model.CurrencyRate;
import com.shipmonk.testingday.rates.model.DailyExchangeRate;
import com.shipmonk.testingday.rates.model.ExchangeRateResponse;
import com.shipmonk.testingday.rates.repository.ExchangeRateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureTestEntityManager
class ExchangeRateITest {

    @Autowired
    private ExchangeRateService service;

    @Autowired
    private ExchangeRateRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;
    @MockBean
    private ExchangeRateProvider provider;

    @Test
    @DisplayName("Integration: Cache MISS should fetch from API, save real rates to DB, and return DTO")
    void shouldFetchAndCache() {
        // GIVEN
        LocalDate today = LocalDate.now();

        List<CurrencyRate> apiRates = Arrays.asList(
            new CurrencyRate("USD", new BigDecimal("1.10")),
            new CurrencyRate("CZK", new BigDecimal("25.50")),
            new CurrencyRate("GBP", new BigDecimal("0.85"))
        );
        DailyExchangeRate apiResponse = new DailyExchangeRate(today, "EUR", apiRates);

        when(provider.fetchRates(today)).thenReturn(apiResponse);

        // WHEN
        ExchangeRateResponse response = service.getRatesForDate(today);

        // THEN:
        assertThat(response.getSource()).isEqualTo("USD");
        assertThat(response.getRates()).containsKey("CZK");
        assertThat(response.getRates()).containsKey("EUR");

        Optional<DailyExchangeRate> savedRate = repository.findByDate(today);
        assertThat(savedRate).isPresent();
        assertThat(savedRate.get().getSource()).isEqualTo("EUR");
        assertThat(savedRate.get().getRates()).hasSize(3);

        verify(provider, times(1)).fetchRates(today);
    }

    @Test
    @DisplayName("Integration: Cache found - should load existing rates from DB without calling API")
    void shouldReturnFromCache() {
        // GIVEN
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<CurrencyRate> dbRates = Arrays.asList(
            new CurrencyRate("EUR", new BigDecimal("0.92")),
            new CurrencyRate("CZK", new BigDecimal("22.45"))
        );
        DailyExchangeRate existingRate = new DailyExchangeRate(yesterday, "USD", dbRates);

        new TransactionTemplate(transactionManager).execute(status -> {
            entityManager.persist(existingRate);
            entityManager.flush();
            return null;
        });

        // WHEN
        ExchangeRateResponse response = service.getRatesForDate(yesterday);

        // THEN:
        assertThat(response.getSource()).isEqualTo("USD");
        assertThat(response.getRates()).hasSize(2);
        assertThat(response.getRates().get("CZK")).isEqualByComparingTo("22.45");

        verify(provider, never()).fetchRates(any());
    }
}
