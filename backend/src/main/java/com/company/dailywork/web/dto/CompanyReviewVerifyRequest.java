package com.company.dailywork.web.dto;

import jakarta.validation.constraints.NotBlank;

public class CompanyReviewVerifyRequest {

    @NotBlank(message = "cannot be empty")
    private String reviewStatus;

    private String remark;

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
