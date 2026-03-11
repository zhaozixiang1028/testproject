package com.company.dailywork.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank(message = "cannot be empty")
    @Size(min = 4, max = 50, message = "length must be between 4 and 50")
    private String username;

    @NotBlank(message = "cannot be empty")
    @Size(min = 6, max = 50, message = "length must be between 6 and 50")
    private String password;

    @NotBlank(message = "cannot be empty")
    @Size(min = 2, max = 50, message = "length must be between 2 and 50")
    private String nickname;

    @NotBlank(message = "cannot be empty")
    private String role;

    @Size(max = 120, message = "length must be <= 120")
    private String employedCompany;

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmployedCompany() {
        return employedCompany;
    }

    public void setEmployedCompany(String employedCompany) {
        this.employedCompany = employedCompany;
    }
}
