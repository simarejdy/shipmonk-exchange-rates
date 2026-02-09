package com.shipmonk.testingday.rates.service;

import com.shipmonk.testingday.rates.model.DailyExchangeRate;

import java.time.LocalDate;

public interface ExchangeRateProvider {

    DailyExchangeRate fetchRates(LocalDate date);
}
