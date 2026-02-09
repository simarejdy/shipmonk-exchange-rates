package com.shipmonk.testingday.rates.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "currency_rate")
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currencyCode;
    private BigDecimal exchangeRate;

    protected CurrencyRate() {
        // For JPA
    }

    public CurrencyRate(String currencyCode, BigDecimal exchangeRate) {
        this.currencyCode = currencyCode;
        this.exchangeRate = exchangeRate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_rate_id", nullable = false)
    @JsonIgnore
    private DailyExchangeRate dailyExchangeRate;

    public String getCurrencyCode() { return currencyCode; }
    public BigDecimal getExchangeRate() { return exchangeRate; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public DailyExchangeRate getDailyExchangeRate() {
        return dailyExchangeRate;
    }

    public void setDailyExchangeRate(DailyExchangeRate dailyExchangeRate) {
        this.dailyExchangeRate = dailyExchangeRate;
    }
}
