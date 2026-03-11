package com.company.dailywork.controller;

import com.company.dailywork.common.model.ApiResponse;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.CompanyReviewService;
import com.company.dailywork.web.dto.CompanyReviewResponse;
import com.company.dailywork.web.dto.CompanyReviewSubmitRequest;
import com.company.dailywork.web.dto.CompanyReviewVerifyRequest;
import com.company.dailywork.web.dto.ReviewAuditLogResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class CompanyReviewController {

    private final CompanyReviewService companyReviewService;

    public CompanyReviewController(CompanyReviewService companyReviewService) {
        this.companyReviewService = companyReviewService;
    }

    @PostMapping("/submit")
    public ApiResponse<CompanyReviewResponse> submit(@RequestBody @Valid CompanyReviewSubmitRequest request,
                                                     @AuthenticationPrincipal SecurityUser user) {
        return ApiResponse.success("Submitted", companyReviewService.submit(request, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<CompanyReviewResponse> update(@PathVariable Long id,
                                                     @RequestBody @Valid CompanyReviewSubmitRequest request,
                                                     @AuthenticationPrincipal SecurityUser user) {
        return ApiResponse.success("Updated", companyReviewService.update(id, request, user));
    }

    @GetMapping
    public ApiResponse<List<CompanyReviewResponse>> list(@RequestParam(required = false) String companyName,
                                                         @AuthenticationPrincipal SecurityUser user) {
        return ApiResponse.success(companyReviewService.list(companyName, user));
    }

    @GetMapping("/company/{companyName}")
    public ApiResponse<List<CompanyReviewResponse>> byCompany(@PathVariable String companyName,
                                                              @AuthenticationPrincipal SecurityUser user) {
        return ApiResponse.success(companyReviewService.list(companyName, user));
    }

    @PutMapping("/verify/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<CompanyReviewResponse> verify(@PathVariable Long id,
                                                     @RequestBody @Valid CompanyReviewVerifyRequest request,
                                                     @AuthenticationPrincipal SecurityUser user) {
        return ApiResponse.success("Verified", companyReviewService.verify(id, request.getReviewStatus(), request.getRemark(), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal SecurityUser user) {
        companyReviewService.delete(id, user);
        return ApiResponse.success("Deleted", null);
    }

    @GetMapping("/{id}/audit-logs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<List<ReviewAuditLogResponse>> auditLogs(@PathVariable Long id) {
        return ApiResponse.success(companyReviewService.listAuditLogs(id));
    }
}
