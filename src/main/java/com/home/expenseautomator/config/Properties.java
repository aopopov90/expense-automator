package com.home.expenseautomator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@Setter
@Getter
public class Properties {
    Integer user0Id;
    Integer user1Id;
    String splitwiseApiKey;
    String splitwiseBaseUrl;
}
