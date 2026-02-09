package com.shipmonk.testingday.rates.client;

import com.shipmonk.testingday.config.FixerFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "fixer-client", url = "${shipmonk.fixer.base-url}", configuration = FixerFeignConfig.class)
public interface FixerClient {

    @GetMapping("/{date}")
    FixerResponseDto getRates(
        @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam("base") String base,
        @RequestParam("symbols") String symbols
    );
}
