package com.company.dailywork.web.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "cannot be empty")
    private String username;

    @NotBlank(message = "cannot be empty")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
