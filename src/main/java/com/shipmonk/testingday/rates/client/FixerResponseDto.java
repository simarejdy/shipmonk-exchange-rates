package com.shipmonk.testingday.rates.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record FixerResponseDto(
    boolean success,
    Long timestamp,
    String base,
    LocalDate date,
    Map<String, BigDecimal> rates,
    FixerError error // <--- We need this nested record
) {
    public record FixerError(int code, String type, String info) {}
}
