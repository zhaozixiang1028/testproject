package com.company.dailywork.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangeMyPasswordRequest {

    @NotBlank(message = "cannot be empty")
    @Size(min = 6, max = 50, message = "length must be between 6 and 50")
    private String oldPassword;

    @NotBlank(message = "cannot be empty")
    @Size(min = 6, max = 50, message = "length must be between 6 and 50")
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
