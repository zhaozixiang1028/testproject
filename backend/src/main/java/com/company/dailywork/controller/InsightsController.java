package com.company.dailywork.controller;

import com.company.dailywork.common.model.ApiResponse;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.InsightsService;
import com.company.dailywork.web.dto.MoodTrendResponse;
import com.company.dailywork.web.dto.RetrospectiveResponse;
import com.company.dailywork.web.dto.RiskRadarResponse;
import com.company.dailywork.web.dto.ReviewAlertResponse;
import com.company.dailywork.web.dto.SkillPortraitResponse;
import com.company.dailywork.web.dto.WeeklyReportResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/weekly-report")
    public ApiResponse<WeeklyReportResponse> weeklyReport(@AuthenticationPrincipal SecurityUser user,
                                                           @RequestParam(defaultValue = "week") String period) {
        return ApiResponse.success(insightsService.weeklyReport(user, period));
    }

    @GetMapping("/mood-trend")
    public ApiResponse<MoodTrendResponse> moodTrend(@AuthenticationPrincipal SecurityUser user,
                                                    @RequestParam(defaultValue = "14") Integer days) {
        return ApiResponse.success(insightsService.moodTrend(user, days));
    }

    @GetMapping("/skills")
    public ApiResponse<SkillPortraitResponse> skillPortrait(@AuthenticationPrincipal SecurityUser user,
                                                            @RequestParam(defaultValue = "30") Integer days) {
        return ApiResponse.success(insightsService.skillPortrait(user, days));
    }

    @GetMapping("/review-alerts")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<ReviewAlertResponse> reviewAlerts(@RequestParam(defaultValue = "14") Integer days) {
        return ApiResponse.success(insightsService.reviewAlerts(days));
    }

    @GetMapping("/risk-radar")
    public ApiResponse<RiskRadarResponse> riskRadar(@AuthenticationPrincipal SecurityUser user,
                                                    @RequestParam(defaultValue = "14") Integer days) {
        return ApiResponse.success(insightsService.riskRadar(user, days));
    }

    @GetMapping("/retrospective")
    public ApiResponse<RetrospectiveResponse> retrospective(@AuthenticationPrincipal SecurityUser user,
                                                            @RequestParam(defaultValue = "week") String period) {
        return ApiResponse.success(insightsService.retrospective(user, period));
    }
}
