package com.shipmonk.testingday.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FixerPropertiesValidator {

    private static final String PLACEHOLDER = "YOUR_API_KEY_HERE";

    private final FixerProperties properties;

    public FixerPropertiesValidator(FixerProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void validate() {
        String apiKey = properties.getApiKey();
        if (!StringUtils.hasText(apiKey) || PLACEHOLDER.equals(apiKey)) {
            throw new IllegalStateException("Fixer API key is missing. Set shipmonk.fixer.api-key.");
        }
    }
}
