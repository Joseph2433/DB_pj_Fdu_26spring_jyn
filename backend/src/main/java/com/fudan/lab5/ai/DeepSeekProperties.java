package com.fudan.lab5.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deepseek")
public record DeepSeekProperties(String apiKey, String baseUrl, String model) {
}
