package com.company.dailywork.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SprintGoalResponse {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String relatedProject;
    private String targetHours;
    private String completedHours;
    private String completionRate;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SprintGoalResponse(Long id, Long userId, String title, String description, String relatedProject,
                              String targetHours, String completedHours, String completionRate, String status,
                              LocalDate startDate, LocalDate endDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.relatedProject = relatedProject;
        this.targetHours = targetHours;
        this.completedHours = completedHours;
        this.completionRate = completionRate;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getRelatedProject() { return relatedProject; }
    public String getTargetHours() { return targetHours; }
    public String getCompletedHours() { return completedHours; }
    public String getCompletionRate() { return completionRate; }
    public String getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
