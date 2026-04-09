package com.company.dailywork.web.dto;

public class AiConfigCheckResponse {

    private boolean keyConfigured;
    private String baseUrl;
    private String model;
    private boolean providerReachable;
    private boolean authPassed;
    private boolean modelAvailable;
    private Integer upstreamStatus;
    private String message;

    public boolean isKeyConfigured() {
        return keyConfigured;
    }

    public void setKeyConfigured(boolean keyConfigured) {
        this.keyConfigured = keyConfigured;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isProviderReachable() {
        return providerReachable;
    }

    public void setProviderReachable(boolean providerReachable) {
        this.providerReachable = providerReachable;
    }

    public boolean isAuthPassed() {
        return authPassed;
    }

    public void setAuthPassed(boolean authPassed) {
        this.authPassed = authPassed;
    }

    public boolean isModelAvailable() {
        return modelAvailable;
    }

    public void setModelAvailable(boolean modelAvailable) {
        this.modelAvailable = modelAvailable;
    }

    public Integer getUpstreamStatus() {
        return upstreamStatus;
    }

    public void setUpstreamStatus(Integer upstreamStatus) {
        this.upstreamStatus = upstreamStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}