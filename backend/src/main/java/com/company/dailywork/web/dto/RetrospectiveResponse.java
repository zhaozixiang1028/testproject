package com.company.dailywork.web.dto;

import java.time.LocalDate;
import java.util.List;

public class RetrospectiveResponse {
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private String theme;
    private String summary;
    private List<String> whatWentWell;
    private List<String> toImprove;
    private List<String> nextActions;

    public RetrospectiveResponse(String period, LocalDate startDate, LocalDate endDate,
                                 String theme, String summary, List<String> whatWentWell,
                                 List<String> toImprove, List<String> nextActions) {
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.theme = theme;
        this.summary = summary;
        this.whatWentWell = whatWentWell;
        this.toImprove = toImprove;
        this.nextActions = nextActions;
    }

    public String getPeriod() { return period; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getTheme() { return theme; }
    public String getSummary() { return summary; }
    public List<String> getWhatWentWell() { return whatWentWell; }
    public List<String> getToImprove() { return toImprove; }
    public List<String> getNextActions() { return nextActions; }
}
