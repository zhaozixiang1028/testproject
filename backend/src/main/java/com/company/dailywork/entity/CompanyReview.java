package com.company.dailywork.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("company_review")
public class CompanyReview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
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
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
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
    public Integer getAnonymousMode() { return anonymousMode; }
    public void setAnonymousMode(Integer anonymousMode) { this.anonymousMode = anonymousMode; }
    public Integer getPublicVisible() { return publicVisible; }
    public void setPublicVisible(Integer publicVisible) { this.publicVisible = publicVisible; }
    public String getReviewContent() { return reviewContent; }
    public void setReviewContent(String reviewContent) { this.reviewContent = reviewContent; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
