package com.company.dailywork.web.dto;

import java.util.List;

public class ReviewAlertResponse {

    public static class AlertItem {
        private String companyName;
        private Integer recentCount;
        private String currentAvg;
        private String previousAvg;
        private String dropRate;
        private String negativeRate;
        private String level;

        public AlertItem(String companyName, Integer recentCount, String currentAvg,
                         String previousAvg, String dropRate, String negativeRate, String level) {
            this.companyName = companyName;
            this.recentCount = recentCount;
            this.currentAvg = currentAvg;
            this.previousAvg = previousAvg;
            this.dropRate = dropRate;
            this.negativeRate = negativeRate;
            this.level = level;
        }

        public String getCompanyName() { return companyName; }
        public Integer getRecentCount() { return recentCount; }
        public String getCurrentAvg() { return currentAvg; }
        public String getPreviousAvg() { return previousAvg; }
        public String getDropRate() { return dropRate; }
        public String getNegativeRate() { return negativeRate; }
        public String getLevel() { return level; }
    }

    private Integer days;
    private Integer totalWarnings;
    private List<AlertItem> warnings;

    public ReviewAlertResponse(Integer days, Integer totalWarnings, List<AlertItem> warnings) {
        this.days = days;
        this.totalWarnings = totalWarnings;
        this.warnings = warnings;
    }

    public Integer getDays() { return days; }
    public Integer getTotalWarnings() { return totalWarnings; }
    public List<AlertItem> getWarnings() { return warnings; }
}
