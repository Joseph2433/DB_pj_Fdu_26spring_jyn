package com.fudan.lab5.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiDraftServiceTest {
    @Test
    void rejectsBlankTopic() {
        AiDraftService service = new AiDraftService(prompt -> List.of("草稿"));

        assertThatThrownBy(() -> service.generate(new AiDraftRequest(" ", "温柔", "短")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("主题不能为空");
    }

    @Test
    void truncatesDraftsToPostLimit() {
        AiDraftService service = new AiDraftService(prompt -> List.of("a".repeat(300)));

        AiDraftResponse response = service.generate(new AiDraftRequest("数据库实验", "轻松", "短"));

        assertThat(response.drafts()).hasSize(1);
        assertThat(response.drafts().get(0)).hasSize(280);
    }
}
