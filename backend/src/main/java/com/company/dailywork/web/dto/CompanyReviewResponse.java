package com.company.dailywork.web.dto;

import java.time.LocalDateTime;

public class CompanyReviewResponse {
    private Long id;
    private Long userId;
    private String username;
    private String companyName;
    private Integer cultureScore;
    private Integer teamScore;
    private Integer growthScore;
    private Integer salaryScore;
    private Integer balanceScore;
    private Integer anonymousMode;
    private Integer publicVisible;
    private String reviewContent;
    private String reviewStatus;
    private LocalDateTime createdAt;

    public CompanyReviewResponse(Long id, Long userId, String username, String companyName,
                                 Integer cultureScore, Integer teamScore, Integer growthScore,
                                 Integer salaryScore, Integer balanceScore, Integer anonymousMode,
                                 Integer publicVisible, String reviewContent, String reviewStatus,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.companyName = companyName;
        this.cultureScore = cultureScore;
        this.teamScore = teamScore;
        this.growthScore = growthScore;
        this.salaryScore = salaryScore;
        this.balanceScore = balanceScore;
        this.anonymousMode = anonymousMode;
        this.publicVisible = publicVisible;
        this.reviewContent = reviewContent;
        this.reviewStatus = reviewStatus;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getCompanyName() { return companyName; }
    public Integer getCultureScore() { return cultureScore; }
    public Integer getTeamScore() { return teamScore; }
    public Integer getGrowthScore() { return growthScore; }
    public Integer getSalaryScore() { return salaryScore; }
    public Integer getBalanceScore() { return balanceScore; }
    public Integer getAnonymousMode() { return anonymousMode; }
    public Integer getPublicVisible() { return publicVisible; }
    public String getReviewContent() { return reviewContent; }
    public String getReviewStatus() { return reviewStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
