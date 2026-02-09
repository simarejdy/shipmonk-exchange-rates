package com.shipmonk.testingday.rates;

import java.time.LocalDate;

public interface ExchangeRateProvider {

    DailyExchangeRate fetchRates(LocalDate date);
}
