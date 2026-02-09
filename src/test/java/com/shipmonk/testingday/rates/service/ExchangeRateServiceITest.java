package com.shipmonk.testingday.rates.service;

import com.shipmonk.testingday.rates.model.DailyExchangeRate;
import com.shipmonk.testingday.rates.model.ExchangeRateResponse;
import com.shipmonk.testingday.rates.repository.ExchangeRateRepository;
import com.shipmonk.testingday.config.FixerProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateServiceITest {

    @Test
    @DisplayName("Cache HIT: Should return data from DB and NOT call provider")
    void shouldReturnFromCache() {
        // GIVEN
        LocalDate today = LocalDate.now();
        DailyExchangeRate cachedRate = new DailyExchangeRate(today, "USD", Collections.emptyList());
        ExchangeRateResponse expectedDto = new ExchangeRateResponse(today, "USD", Collections.emptyMap());

        FakeRepository repository = new FakeRepository(Optional.of(cachedRate));
        FakeProvider provider = new FakeProvider();
        FakeCalculator calculator = new FakeCalculator(expectedDto);

        ExchangeRateService service = new ExchangeRateService(repository, provider, calculator, testProperties());

        // WHEN
        ExchangeRateResponse result = service.getRatesForDate(today);

        // THEN
        assertThat(result).isEqualTo(expectedDto);
        assertThat(provider.fetchCalls).isZero();
        assertThat(repository.persistCalls).isZero();
        assertThat(repository.findCalls).isEqualTo(1);
    }

    @Test
    @DisplayName("Cache MISS: Should call provider and save to DB")
    void shouldFetchFromProvider() {
        // GIVEN
        LocalDate today = LocalDate.now();
        DailyExchangeRate newRate = new DailyExchangeRate(today, "USD", Collections.emptyList());
        ExchangeRateResponse expectedDto = new ExchangeRateResponse(today, "USD", Collections.emptyMap());

        FakeRepository repository = new FakeRepository(Optional.empty());
        FakeProvider provider = new FakeProvider(newRate);
        FakeCalculator calculator = new FakeCalculator(expectedDto);

        ExchangeRateService service = new ExchangeRateService(repository, provider, calculator, testProperties());

        // WHEN
        ExchangeRateResponse result = service.getRatesForDate(today);

        // THEN
        assertThat(result).isEqualTo(expectedDto);
        assertThat(provider.fetchCalls).isEqualTo(1);
        assertThat(repository.persistCalls).isEqualTo(1);
        assertThat(repository.findCalls).isEqualTo(1);
    }

    @Test
    @DisplayName("Race condition: On unique conflict, should read from DB")
    void shouldFallbackToDbOnConflict() {
        // GIVEN
        LocalDate today = LocalDate.now();
        DailyExchangeRate fetched = new DailyExchangeRate(today, "USD", Collections.emptyList());
        DailyExchangeRate existing = new DailyExchangeRate(today, "USD", Collections.emptyList());
        ExchangeRateResponse expectedDto = new ExchangeRateResponse(today, "USD", Collections.emptyMap());

        FakeRepository repository = new FakeRepository(Optional.of(existing));
        repository.throwOnFlush = true;
        repository.missFirstLookup = true;

        FakeProvider provider = new FakeProvider(fetched);
        FakeCalculator calculator = new FakeCalculator(expectedDto);

        ExchangeRateService service = new ExchangeRateService(repository, provider, calculator, testProperties());

        // WHEN
        ExchangeRateResponse result = service.getRatesForDate(today);

        // THEN
        assertThat(result).isEqualTo(expectedDto);
        assertThat(provider.fetchCalls).isEqualTo(1);
        assertThat(repository.flushCalls).isEqualTo(1);
        assertThat(repository.findCalls).isEqualTo(2); // initial miss + fallback read
    }

    private static final class FakeRepository extends ExchangeRateRepository {
        private Optional<DailyExchangeRate> stored;
        private int findCalls = 0;
        private int persistCalls = 0;
        private int flushCalls = 0;
        private boolean throwOnFlush = false;
        private boolean missFirstLookup = false;

        private FakeRepository(Optional<DailyExchangeRate> stored) {
            this.stored = stored;
        }

        @Override
        public Optional<DailyExchangeRate> findByDate(LocalDate date, String source) {
            findCalls++;
            if (missFirstLookup && findCalls == 1) {
                return Optional.empty();
            }
            return stored.filter(rate ->
                rate.getDate().equals(date) && rate.getSource().equalsIgnoreCase(source));
        }

        @Override
        public void persist(DailyExchangeRate rate) {
            persistCalls++;
            stored = Optional.of(rate);
        }

        @Override
        public void flush() {
            flushCalls++;
            if (throwOnFlush) {
                throw new DataIntegrityViolationException("Unique constraint violation");
            }
        }

    }

    private static FixerProperties testProperties() {
        FixerProperties properties = new FixerProperties();
        properties.setBaseCurrency("USD");
        return properties;
    }

    private static final class FakeProvider implements ExchangeRateProvider {
        private final DailyExchangeRate response;
        private int fetchCalls = 0;

        private FakeProvider() {
            this.response = null;
        }

        private FakeProvider(DailyExchangeRate response) {
            this.response = response;
        }

        @Override
        public DailyExchangeRate fetchRates(LocalDate date) {
            fetchCalls++;
            return response;
        }
    }

    private static final class FakeCalculator extends ExchangeRateCalculator {
        private final ExchangeRateResponse response;

        private FakeCalculator(ExchangeRateResponse response) {
            this.response = response;
        }

        @Override
        public ExchangeRateResponse convertToDto(DailyExchangeRate original, String targetCurrency) {
            return response;
        }
    }
}
