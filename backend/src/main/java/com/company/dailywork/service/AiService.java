package com.company.dailywork.service;

import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.web.dto.AiChatRequest;
import reactor.core.publisher.Flux;

public interface AiService {
    Flux<String> streamChat(SecurityUser user, AiChatRequest request);
}
