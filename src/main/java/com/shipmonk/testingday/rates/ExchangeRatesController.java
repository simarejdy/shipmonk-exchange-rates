package com.shipmonk.testingday.rates;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/rates")
public class ExchangeRatesController {

    private final ExchangeRateService service;

    public ExchangeRatesController(ExchangeRateService service) {
        this.service = service;
    }

    @GetMapping("/{date}")
    public ResponseEntity<DailyExchangeRate> getRates(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().build();
        }

        DailyExchangeRate rate = service.getRatesForDate(date);

        return ResponseEntity.ok(rate);
    }
}
