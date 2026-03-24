package com.company.dailywork.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class AiChatRequest {

    public static class Message {
        @NotBlank(message = "role cannot be empty")
        private String role;

        @NotBlank(message = "content cannot be empty")
        private String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    private String sessionId;
    private List<Message> messages;

    @NotBlank(message = "prompt cannot be empty")
    private String prompt;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
