package com.shipmonk.testingday.rates;

import com.shipmonk.testingday.config.FixerFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * ROHLIK STYLE: Declarative Client.
 * URL is read from config (allowing easy switch between HTTP/HTTPS or Free/Paid).
 */
@FeignClient(
    name = "fixer-client",
    url = "${shipmonk.fixer.url:http://data.fixer.io/api}", // Default to HTTP (Free Tier)
    configuration = FixerFeignConfig.class // Applies the API Key interceptor
)
public interface FixerClient {

    @GetMapping("/{date}")
    FixerResponseDto getRates(
        @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam("base") String base,
        @RequestParam("symbols") String symbols
    );
}
