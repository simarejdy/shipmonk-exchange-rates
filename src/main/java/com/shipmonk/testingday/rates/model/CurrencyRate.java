package com.shipmonk.testingday.rates.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "currency_rate")
@Setter
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currencyCode;
    private BigDecimal exchangeRate;

    public CurrencyRate() {}

    public CurrencyRate(String currencyCode, BigDecimal exchangeRate) {
        this.currencyCode = currencyCode;
        this.exchangeRate = exchangeRate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_rate_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private DailyExchangeRate dailyExchangeRate;

    public String getCurrencyCode() { return currencyCode; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
}
