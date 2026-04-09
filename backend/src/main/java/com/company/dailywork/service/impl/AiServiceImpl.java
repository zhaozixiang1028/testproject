package com.company.dailywork.service.impl;

import com.company.dailywork.config.AiProperties;
import com.company.dailywork.service.AiService;
import com.company.dailywork.web.dto.AiChatRequest;
import com.company.dailywork.web.dto.AiConfigCheckResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

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
    public Flux<String> streamChat(AiChatRequest request) {
        String apiKey = resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
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
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(payload)
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorMap(WebClientResponseException.class,
                        ex -> new IllegalStateException(buildUpstreamErrorMessage(ex)))
                .transform(this::extractTokenFlux);
    }

    @Override
    public Mono<AiConfigCheckResponse> checkConfig() {
        String apiKey = resolveApiKey();
        AiConfigCheckResponse response = new AiConfigCheckResponse();
        response.setBaseUrl(aiProperties.getBaseUrl());
        response.setModel(aiProperties.getModel());
        response.setKeyConfigured(apiKey != null && !apiKey.isBlank());

        if (!response.isKeyConfigured()) {
            response.setProviderReachable(false);
            response.setAuthPassed(false);
            response.setModelAvailable(false);
            response.setMessage("未配置 AI Key，请设置 APP_AI_API_KEY 或 DASHSCOPE_API_KEY");
            return Mono.just(response);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", aiProperties.getModel());
        payload.put("stream", true);
        payload.put("max_tokens", 1);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> checkMessage = new HashMap<>();
        checkMessage.put("role", "user");
        checkMessage.put("content", "ping");
        messages.add(checkMessage);
        payload.put("messages", messages);

        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(payload)
                .retrieve()
                .bodyToFlux(String.class)
                .next()
                .map(body -> {
                    response.setProviderReachable(true);
                    response.setAuthPassed(true);
                    response.setModelAvailable(true);
                    response.setUpstreamStatus(200);
                    response.setMessage("AI 配置检查通过（流式链路）");
                    return response;
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    int status = ex.getStatusCode().value();
                    String detail = extractUpstreamMessage(ex.getResponseBodyAsString());
                    response.setProviderReachable(true);
                    response.setUpstreamStatus(status);
                    if (status == 401) {
                        response.setAuthPassed(false);
                        response.setModelAvailable(false);
                        response.setMessage("鉴权失败(401): " + (detail == null ? "API Key 无效或已过期" : detail));
                    } else if (status == 404) {
                        response.setAuthPassed(true);
                        response.setModelAvailable(false);
                        response.setMessage("模型或网关路径不可用(404): " + (detail == null ? "请检查模型名和 base-url" : detail));
                    } else if (status == 429) {
                        response.setAuthPassed(true);
                        response.setModelAvailable(true);
                        response.setMessage("请求被限流(429): " + (detail == null ? "请稍后重试" : detail));
                    } else {
                        response.setAuthPassed(status != 401 && status != 403);
                        response.setModelAvailable(status < 500);
                        response.setMessage("上游返回异常(" + status + "): " + (detail == null ? "请检查网关和模型配置" : detail));
                    }
                    return Mono.just(response);
                })
                .onErrorResume(ex -> {
                    response.setProviderReachable(false);
                    response.setAuthPassed(false);
                    response.setModelAvailable(false);
                    response.setMessage("网络不可达或请求超时: " + ex.getMessage());
                    return Mono.just(response);
                });
    }

    private String resolveApiKey() {
        String appApiKey = normalize(System.getenv("APP_AI_API_KEY"));
        if (appApiKey != null) {
            return appApiKey;
        }

        String dashscopeApiKey = normalize(System.getenv("DASHSCOPE_API_KEY"));
        if (dashscopeApiKey != null) {
            return dashscopeApiKey;
        }

        return normalize(aiProperties.getApiKey());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String buildUpstreamErrorMessage(WebClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        String parsedMessage = extractUpstreamMessage(body);

        if (ex.getStatusCode().value() == 401) {
            String detail = (parsedMessage != null && !parsedMessage.isBlank())
                    ? parsedMessage
                    : "请检查 APP_AI_API_KEY 或 DASHSCOPE_API_KEY 与 DashScope 网关配置";
            return "AI 上游鉴权失败(401): " + detail;
        }

        if (parsedMessage != null && !parsedMessage.isBlank()) {
            return "AI 上游错误(" + ex.getStatusCode().value() + "): " + parsedMessage;
        }

        return "AI 上游错误(" + ex.getStatusCode().value() + ")";
    }

    private String extractUpstreamMessage(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            String parsedMessage = root.path("message").asText(null);
            if (parsedMessage == null || parsedMessage.isBlank()) {
                parsedMessage = root.path("error").path("message").asText(null);
            }
            return (parsedMessage == null || parsedMessage.isBlank()) ? null : parsedMessage;
        } catch (Exception ignored) {
            return body;
        }
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

        if (buffer.length() > 0) {
            String lastLine = buffer.toString().trim();
            buffer.setLength(0);
            handleSseLine(lastLine, sink);
        }
    }

    private void handleSseLine(String line, FluxSink<String> sink) {
        if (line.isBlank() || line.startsWith("event:") || line.startsWith(":")) {
            return;
        }

        String payload = line;
        if (payload.startsWith("data:")) {
            payload = payload.substring(5).trim();
        }

        if (payload.isEmpty() || "[DONE]".equals(payload)) {
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return;
            }

            JsonNode firstChoice = choices.path(0);
            JsonNode deltaContent = firstChoice.path("delta").path("content");
            if (!deltaContent.isMissingNode() && !deltaContent.isNull()) {
                String token = deltaContent.asText();
                if (!token.isBlank()) {
                    sink.next(token);
                }
                return;
            }

            JsonNode messageContent = firstChoice.path("message").path("content");
            if (!messageContent.isMissingNode() && !messageContent.isNull()) {
                String token = messageContent.asText();
                if (!token.isBlank()) {
                    sink.next(token);
                }
            }
        } catch (Exception ignored) {
            // Ignore malformed stream lines from upstream chunks.
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
