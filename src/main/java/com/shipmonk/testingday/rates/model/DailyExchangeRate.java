package com.shipmonk.testingday.rates.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable; // Or use JSpecify if you added the dependency

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "daily_exchange_rate",
    uniqueConstraints = @UniqueConstraint(name = "uk_date_source", columnNames = {"date", "source"})
)
public class DailyExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @NotNull
    @Column(nullable = false)
    private String source;

    @OneToMany(mappedBy = "dailyExchangeRate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CurrencyRate> rates = new ArrayList<>();

    protected DailyExchangeRate() {
        // For JPA
    }

    public DailyExchangeRate(LocalDate date, String source, List<CurrencyRate> rates) {
        this.date = date;
        this.source = source;
        this.rates = (rates != null) ? rates : new ArrayList<>();

        this.rates.forEach(rate -> rate.setDailyExchangeRate(this));
    }

    public Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<CurrencyRate> getRates() {
        return rates;
    }

    public void setRates(List<CurrencyRate> rates) {
        this.rates = (rates != null) ? rates : new ArrayList<>();
        this.rates.forEach(rate -> rate.setDailyExchangeRate(this));
    }
}
