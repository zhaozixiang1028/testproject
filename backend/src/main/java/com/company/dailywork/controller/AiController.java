package com.company.dailywork.controller;

import com.company.dailywork.common.model.ApiResponse;
import com.company.dailywork.service.AiService;
import com.company.dailywork.web.dto.AiChatRequest;
import com.company.dailywork.web.dto.AiConfigCheckResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody @Valid AiChatRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        aiService.streamChat(request).subscribe(
                token -> {
                    try {
                        emitter.send(SseEmitter.event().data(token));
                    } catch (IOException ex) {
                        emitter.completeWithError(ex);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );
        return emitter;
    }

    @GetMapping("/config/check")
    public ApiResponse<AiConfigCheckResponse> checkConfig() {
        AiConfigCheckResponse data = aiService.checkConfig().block();
        return ApiResponse.success(data);
    }
}
