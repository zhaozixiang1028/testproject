package com.company.dailywork.service;

import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.web.dto.CompanyReviewResponse;
import com.company.dailywork.web.dto.CompanyReviewSubmitRequest;
import com.company.dailywork.web.dto.ReviewAuditLogResponse;

import java.util.List;

public interface CompanyReviewService {
    CompanyReviewResponse submit(CompanyReviewSubmitRequest request, SecurityUser user);
    CompanyReviewResponse update(Long reviewId, CompanyReviewSubmitRequest request, SecurityUser user);
    List<CompanyReviewResponse> list(String companyName, SecurityUser user);
    CompanyReviewResponse verify(Long reviewId, String reviewStatus, String remark, SecurityUser operator);
    void delete(Long reviewId, SecurityUser operator);
    List<ReviewAuditLogResponse> listAuditLogs(Long reviewId);
}
