package com.company.dailywork.web.dto;

import java.math.BigDecimal;
import java.util.List;

public class WorkLogStatisticsResponse {
    private String period;
    private BigDecimal totalHours;
    private Integer totalLogs;
    private List<ProjectHoursItem> projectHours;

    public WorkLogStatisticsResponse(String period, BigDecimal totalHours, Integer totalLogs, List<ProjectHoursItem> projectHours) {
        this.period = period;
        this.totalHours = totalHours;
        this.totalLogs = totalLogs;
        this.projectHours = projectHours;
    }

    public String getPeriod() { return period; }
    public BigDecimal getTotalHours() { return totalHours; }
    public Integer getTotalLogs() { return totalLogs; }
    public List<ProjectHoursItem> getProjectHours() { return projectHours; }

    public static class ProjectHoursItem {
        private String projectName;
        private BigDecimal hours;

        public ProjectHoursItem(String projectName, BigDecimal hours) {
            this.projectName = projectName;
            this.hours = hours;
        }

        public String getProjectName() { return projectName; }
        public BigDecimal getHours() { return hours; }
    }
}
