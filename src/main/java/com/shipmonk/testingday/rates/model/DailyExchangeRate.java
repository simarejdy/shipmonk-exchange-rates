package com.shipmonk.testingday.rates.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_exchange_rates")
public class DailyExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "daily_exchange_rate_seq")
    @SequenceGenerator(name = "daily_exchange_rate_seq", allocationSize = 50)
    private Long id;

    @Column(name = "rate_date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "source_currency", nullable = false)
    private String source;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "daily_rate_id")
    private List<CurrencyRate> rates = new ArrayList<>();

    protected DailyExchangeRate() {}

    public DailyExchangeRate(LocalDate date, String source, List<CurrencyRate> rates) {
        this.date = date;
        this.source = source;
        this.rates = rates;
    }

    public LocalDate getDate() { return date; }
    public String getSource() { return source; }
    public List<CurrencyRate> getRates() { return rates; }
}
