package com.company.dailywork.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CompanyReviewSubmitRequest {
    @NotBlank(message = "cannot be empty")
    private String companyName;

    @NotNull(message = "cannot be empty") @Min(1) @Max(5)
    private Integer cultureScore;
    @NotNull(message = "cannot be empty") @Min(1) @Max(5)
    private Integer teamScore;
    @NotNull(message = "cannot be empty") @Min(1) @Max(5)
    private Integer growthScore;
    @NotNull(message = "cannot be empty") @Min(1) @Max(5)
    private Integer salaryScore;
    @NotNull(message = "cannot be empty") @Min(1) @Max(5)
    private Integer balanceScore;

    private Boolean anonymousMode = true;
    private Boolean publicVisible = false;
    private String reviewContent;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public Integer getCultureScore() { return cultureScore; }
    public void setCultureScore(Integer cultureScore) { this.cultureScore = cultureScore; }
    public Integer getTeamScore() { return teamScore; }
    public void setTeamScore(Integer teamScore) { this.teamScore = teamScore; }
    public Integer getGrowthScore() { return growthScore; }
    public void setGrowthScore(Integer growthScore) { this.growthScore = growthScore; }
    public Integer getSalaryScore() { return salaryScore; }
    public void setSalaryScore(Integer salaryScore) { this.salaryScore = salaryScore; }
    public Integer getBalanceScore() { return balanceScore; }
    public void setBalanceScore(Integer balanceScore) { this.balanceScore = balanceScore; }
    public Boolean getAnonymousMode() { return anonymousMode; }
    public void setAnonymousMode(Boolean anonymousMode) { this.anonymousMode = anonymousMode; }
    public Boolean getPublicVisible() { return publicVisible; }
    public void setPublicVisible(Boolean publicVisible) { this.publicVisible = publicVisible; }
    public String getReviewContent() { return reviewContent; }
    public void setReviewContent(String reviewContent) { this.reviewContent = reviewContent; }
}
