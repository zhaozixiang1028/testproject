package com.company.dailywork.web.dto;

import java.time.LocalDateTime;

public class ReviewAuditLogResponse {
    private Long id;
    private Long reviewId;
    private Long operatorUserId;
    private String operatorUsername;
    private String oldStatus;
    private String newStatus;
    private String remark;
    private LocalDateTime createdAt;

    public ReviewAuditLogResponse(Long id, Long reviewId, Long operatorUserId, String operatorUsername,
                                  String oldStatus, String newStatus, String remark, LocalDateTime createdAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.operatorUserId = operatorUserId;
        this.operatorUsername = operatorUsername;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.remark = remark;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getReviewId() { return reviewId; }
    public Long getOperatorUserId() { return operatorUserId; }
    public String getOperatorUsername() { return operatorUsername; }
    public String getOldStatus() { return oldStatus; }
    public String getNewStatus() { return newStatus; }
    public String getRemark() { return remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
