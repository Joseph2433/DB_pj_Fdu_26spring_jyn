package com.fudan.lab5.ai;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class DeepSeekHttpClient implements DeepSeekClient {
    private final DeepSeekProperties properties;
    private final RestClient restClient;

    public DeepSeekHttpClient(DeepSeekProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .build();
    }

    @Override
    public List<String> generateDrafts(String prompt) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalArgumentException("DeepSeek API Key 未配置");
        }
        ChatResponse response = restClient.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + properties.apiKey())
            .body(Map.of(
                "model", properties.model(),
                "messages", List.of(
                    Map.of("role", "system", "content", "你是朋友圈文案助手，只返回 1 到 3 条中文短文案，每条一行。"),
                    Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.8
            ))
            .retrieve()
            .body(ChatResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalArgumentException("DeepSeek 未返回文案");
        }
        String content = response.choices().get(0).message().content();
        return content.lines()
            .map(line -> line.replaceFirst("^\\d+[.、]\\s*", "").trim())
            .filter(line -> !line.isEmpty())
            .limit(3)
            .toList();
    }

    record ChatResponse(List<Choice> choices) {
    }

    record Choice(Message message) {
    }

    record Message(String content) {
    }
}
