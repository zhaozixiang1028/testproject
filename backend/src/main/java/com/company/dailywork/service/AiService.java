package com.company.dailywork.service;

import com.company.dailywork.web.dto.AiChatRequest;
import com.company.dailywork.web.dto.AiConfigCheckResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AiService {
    Flux<String> streamChat(AiChatRequest request);

    Mono<AiConfigCheckResponse> checkConfig();
}
