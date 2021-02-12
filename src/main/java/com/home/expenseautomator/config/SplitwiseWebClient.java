package com.home.expenseautomator.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SplitwiseWebClient {

    private final Properties properties;

    @Bean
    public WebClient swWebClient() {
        return WebClient.builder()
                .baseUrl(properties.getSplitwiseBaseUrl())
                .defaultHeaders(header -> header.setBearerAuth(properties.getSplitwiseApiKey()))
                .build();
    }
}
