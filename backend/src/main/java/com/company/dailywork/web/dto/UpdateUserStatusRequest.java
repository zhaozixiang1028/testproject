package com.company.dailywork.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusRequest {

    @NotNull(message = "cannot be empty")
    @Min(value = 0, message = "must be 0 or 1")
    @Max(value = 1, message = "must be 0 or 1")
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
