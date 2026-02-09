package com.shipmonk.testingday.rates.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record FixerResponseDto(
    boolean success,
    long timestamp,
    String base,
    LocalDate date,
    Map<String, BigDecimal> rates,
    @JsonProperty("error") FixerError error
) {
    public record FixerError(int code, String type) {}
}
