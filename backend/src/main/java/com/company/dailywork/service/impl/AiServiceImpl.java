package com.company.dailywork.service.impl;

import com.company.dailywork.config.AiProperties;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.AiService;
import com.company.dailywork.web.dto.AiChatRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    private final AiProperties aiProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AiServiceImpl(AiProperties aiProperties, WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.webClient = webClientBuilder.baseUrl(aiProperties.getBaseUrl()).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Flux<String> streamChat(SecurityUser user, AiChatRequest request) {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            return Flux.error(new IllegalArgumentException("AI API key is not configured"));
        }

        List<Map<String, String>> messages = new ArrayList<>();
        if (request.getMessages() != null) {
            for (AiChatRequest.Message message : request.getMessages()) {
                if (message.getRole() == null || message.getRole().isBlank()) {
                    continue;
                }
                if (message.getContent() == null || message.getContent().isBlank()) {
                    continue;
                }
                String role = normalizeRole(message.getRole());
                if (role == null) {
                    continue;
                }
                Map<String, String> line = new HashMap<>();
                line.put("role", role);
                line.put("content", message.getContent());
                messages.add(line);
            }
        }

        Map<String, String> finalPrompt = new HashMap<>();
        finalPrompt.put("role", "user");
        finalPrompt.put("content", request.getPrompt());
        messages.add(finalPrompt);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", aiProperties.getModel());
        payload.put("stream", true);
        payload.put("messages", messages);

        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey())
                .bodyValue(payload)
                .retrieve()
                .bodyToFlux(String.class)
            .onErrorMap(WebClientResponseException.Unauthorized.class,
                ex -> new IllegalStateException("AI 服务鉴权失败，请检查 APP_AI_API_KEY 是否有效"))
                .transform(this::extractTokenFlux);
    }

    private Flux<String> extractTokenFlux(Flux<String> rawFlux) {
        return Flux.create(sink -> {
            StringBuilder buffer = new StringBuilder();
            rawFlux.subscribe(
                    chunk -> {
                        buffer.append(chunk);
                        flushBuffer(buffer, sink);
                    },
                    sink::error,
                    () -> {
                        flushBuffer(buffer, sink);
                        sink.complete();
                    }
            );
        });
    }

    private void flushBuffer(StringBuilder buffer, FluxSink<String> sink) {
        int index;
        while ((index = buffer.indexOf("\n")) >= 0) {
            String line = buffer.substring(0, index).trim();
            buffer.delete(0, index + 1);
            handleSseLine(line, sink);
        }
    }

    private void handleSseLine(String line, FluxSink<String> sink) {
        if (!line.startsWith("data:")) {
            return;
        }

        String data = line.substring(5).trim();
        if (data.isEmpty() || "[DONE]".equals(data)) {
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode deltaContent = root.path("choices").path(0).path("delta").path("content");
            if (!deltaContent.isMissingNode() && !deltaContent.isNull()) {
                String token = deltaContent.asText();
                if (!token.isBlank()) {
                    sink.next(token);
                }
            }
        } catch (Exception ignored) {
            // Ignore malformed SSE lines from upstream chunks.
        }
    }

    private String normalizeRole(String role) {
        String value = role.toLowerCase();
        if ("user".equals(value) || "assistant".equals(value) || "system".equals(value)) {
            return value;
        }
        return null;
    }
}
