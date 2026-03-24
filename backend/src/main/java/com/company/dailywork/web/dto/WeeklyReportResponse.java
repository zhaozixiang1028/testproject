package com.company.dailywork.web.dto;

import java.time.LocalDate;
import java.util.List;

public class WeeklyReportResponse {
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalLogs;
    private String totalHours;
    private String summary;
    private List<String> highlights;
    private List<String> risks;
    private List<String> nextPlans;

    public WeeklyReportResponse(String period, LocalDate startDate, LocalDate endDate,
                                Integer totalLogs, String totalHours, String summary,
                                List<String> highlights, List<String> risks, List<String> nextPlans) {
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalLogs = totalLogs;
        this.totalHours = totalHours;
        this.summary = summary;
        this.highlights = highlights;
        this.risks = risks;
        this.nextPlans = nextPlans;
    }

    public String getPeriod() { return period; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Integer getTotalLogs() { return totalLogs; }
    public String getTotalHours() { return totalHours; }
    public String getSummary() { return summary; }
    public List<String> getHighlights() { return highlights; }
    public List<String> getRisks() { return risks; }
    public List<String> getNextPlans() { return nextPlans; }
}
