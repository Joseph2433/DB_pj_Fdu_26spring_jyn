package com.fudan.lab5.ai;

import com.fudan.lab5.common.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiDraftService aiDraftService;

    public AiController(AiDraftService aiDraftService) {
        this.aiDraftService = aiDraftService;
    }

    @PostMapping("/post-drafts")
    public ApiResponse<AiDraftResponse> postDrafts(@RequestBody AiDraftRequest request) {
        return ApiResponse.ok(aiDraftService.generate(request));
    }
}
