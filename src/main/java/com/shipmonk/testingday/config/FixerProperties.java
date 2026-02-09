package com.shipmonk.testingday.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "shipmonk.fixer")
public class FixerProperties {

    private String apiKey;
    private String url;
    private boolean allowCustomCurrencyBase = false;
    private List<String> targetCurrencies = List.of("USD", "CZK", "GBP");

}
