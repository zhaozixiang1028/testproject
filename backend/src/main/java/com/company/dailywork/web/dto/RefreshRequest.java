package com.company.dailywork.web.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequest {

    @NotBlank(message = "cannot be empty")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
