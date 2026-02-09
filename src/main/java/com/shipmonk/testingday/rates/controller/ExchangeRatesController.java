package com.shipmonk.testingday.rates.controller;

import com.shipmonk.testingday.rates.model.ExchangeRateResponse;
import com.shipmonk.testingday.rates.service.ExchangeRateService;
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
    public ResponseEntity<ExchangeRateResponse> getRates(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().build();
        }

        ExchangeRateResponse response = service.getRatesForDate(date);

        return ResponseEntity.ok(response);
    }
}
