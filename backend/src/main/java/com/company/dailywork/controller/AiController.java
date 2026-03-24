package com.company.dailywork.controller;

import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.AiService;
import com.company.dailywork.web.dto.AiChatRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@AuthenticationPrincipal SecurityUser user,
                                   @RequestBody @Valid AiChatRequest request) {
        return aiService.streamChat(user, request);
    }
}
