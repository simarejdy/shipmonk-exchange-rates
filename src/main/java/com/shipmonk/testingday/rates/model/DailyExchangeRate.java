package com.shipmonk.testingday.rates.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable; // Or use JSpecify if you added the dependency

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "daily_exchange_rate")
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

    @OneToMany(mappedBy = "dailyExchangeRate", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CurrencyRate> rates = new ArrayList<>();

    public DailyExchangeRate(LocalDate date, String source, List<CurrencyRate> rates) {
        this.date = date;
        this.source = source;
        this.rates = (rates != null) ? rates : new ArrayList<>();

        this.rates.forEach(rate -> rate.setDailyExchangeRate(this));
    }
}
