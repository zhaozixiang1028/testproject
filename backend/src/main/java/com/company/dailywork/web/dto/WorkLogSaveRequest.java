package com.company.dailywork.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkLogSaveRequest {
    private Long id;

    @NotNull(message = "cannot be empty")
    private LocalDate workDate;

    @NotBlank(message = "cannot be empty")
    private String title;

    private String content;
    private String projectName;
    private String taskType;
    private String priorityLevel;
    @Min(value = 1, message = "mood score should be between 1 and 5")
    @Max(value = 5, message = "mood score should be between 1 and 5")
    private Integer moodScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String tags;
    private String attachmentUrls;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }
    public Integer getMoodScore() { return moodScore; }
    public void setMoodScore(Integer moodScore) { this.moodScore = moodScore; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getAttachmentUrls() { return attachmentUrls; }
    public void setAttachmentUrls(String attachmentUrls) { this.attachmentUrls = attachmentUrls; }
}
