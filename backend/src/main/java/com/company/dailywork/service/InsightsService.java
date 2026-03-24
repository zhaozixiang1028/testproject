package com.company.dailywork.service;

import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.web.dto.MoodTrendResponse;
import com.company.dailywork.web.dto.RetrospectiveResponse;
import com.company.dailywork.web.dto.RiskRadarResponse;
import com.company.dailywork.web.dto.ReviewAlertResponse;
import com.company.dailywork.web.dto.SkillPortraitResponse;
import com.company.dailywork.web.dto.WeeklyReportResponse;

public interface InsightsService {
    WeeklyReportResponse weeklyReport(SecurityUser user, String period);
    MoodTrendResponse moodTrend(SecurityUser user, Integer days);
    SkillPortraitResponse skillPortrait(SecurityUser user, Integer days);
    ReviewAlertResponse reviewAlerts(Integer days);
    RiskRadarResponse riskRadar(SecurityUser user, Integer days);
    RetrospectiveResponse retrospective(SecurityUser user, String period);
}
