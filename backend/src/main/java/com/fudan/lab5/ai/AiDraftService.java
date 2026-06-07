package com.fudan.lab5.ai;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiDraftService {
    private static final int POST_LIMIT = 280;
    private final DeepSeekClient deepSeekClient;

    public AiDraftService(DeepSeekClient deepSeekClient) {
        this.deepSeekClient = deepSeekClient;
    }

    public AiDraftResponse generate(AiDraftRequest request) {
        if (request.topic() == null || request.topic().trim().isEmpty()) {
            throw new IllegalArgumentException("主题不能为空");
        }
        String prompt = "请生成 1 到 3 条朋友圈文案。主题：" + request.topic().trim()
            + "；语气：" + fallback(request.tone(), "自然")
            + "；长度：" + fallback(request.lengthPreference(), "短")
            + "。每条不超过 280 字，不要解释。";
        List<String> drafts = deepSeekClient.generateDrafts(prompt).stream()
            .map(String::trim)
            .filter(text -> !text.isEmpty())
            .map(text -> text.length() > POST_LIMIT ? text.substring(0, POST_LIMIT) : text)
            .limit(3)
            .toList();
        return new AiDraftResponse(drafts);
    }

    private String fallback(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
