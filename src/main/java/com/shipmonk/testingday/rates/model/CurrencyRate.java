package com.shipmonk.testingday.rates.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "currency_rates")
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currency_rate_seq")
    @SequenceGenerator(name = "currency_rate_seq", allocationSize = 50)
    private Long id;

    private String currencyCode;
    private BigDecimal exchangeRate;

    public CurrencyRate() {}

    public CurrencyRate(String currencyCode, BigDecimal exchangeRate) {
        this.currencyCode = currencyCode;
        this.exchangeRate = exchangeRate;
    }

    public String getCurrencyCode() { return currencyCode; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
}
