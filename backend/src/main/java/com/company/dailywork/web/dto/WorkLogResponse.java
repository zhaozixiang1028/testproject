package com.company.dailywork.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkLogResponse {
    private Long id;
    private Long userId;
    private LocalDate workDate;
    private String title;
    private String content;
    private String projectName;
    private String taskType;
    private String priorityLevel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal workHours;
    private String tags;
    private String attachmentUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkLogResponse(Long id, Long userId, LocalDate workDate, String title, String content, String projectName,
                           String taskType, String priorityLevel, LocalDateTime startTime, LocalDateTime endTime,
                           BigDecimal workHours, String tags, String attachmentUrls,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.workDate = workDate;
        this.title = title;
        this.content = content;
        this.projectName = projectName;
        this.taskType = taskType;
        this.priorityLevel = priorityLevel;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workHours = workHours;
        this.tags = tags;
        this.attachmentUrls = attachmentUrls;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public LocalDate getWorkDate() { return workDate; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getProjectName() { return projectName; }
    public String getTaskType() { return taskType; }
    public String getPriorityLevel() { return priorityLevel; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public BigDecimal getWorkHours() { return workHours; }
    public String getTags() { return tags; }
    public String getAttachmentUrls() { return attachmentUrls; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
