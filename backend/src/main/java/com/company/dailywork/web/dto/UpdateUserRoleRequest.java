package com.company.dailywork.web.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserRoleRequest {

    @NotBlank(message = "cannot be empty")
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
