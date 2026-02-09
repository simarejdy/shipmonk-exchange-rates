package com.shipmonk.testingday.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class FixerFeignConfig {

    @Value("${shipmonk.fixer.api-key}")
    private String apiKey;

    /**
     * Interceptor that adds ?access_key=YOUR_KEY to every request.
     * This makes the client "scalable" because the Service doesn't need to know about auth.
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.query("access_key", apiKey);
        };
    }
}
