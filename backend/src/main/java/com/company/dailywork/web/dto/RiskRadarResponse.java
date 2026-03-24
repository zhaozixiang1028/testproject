package com.company.dailywork.web.dto;

import java.util.List;

public class RiskRadarResponse {

    public static class RiskItem {
        private String source;
        private String level;
        private String title;
        private String message;
        private String metric;

        public RiskItem(String source, String level, String title, String message, String metric) {
            this.source = source;
            this.level = level;
            this.title = title;
            this.message = message;
            this.metric = metric;
        }

        public String getSource() { return source; }
        public String getLevel() { return level; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getMetric() { return metric; }
    }

    private Integer days;
    private String overallLevel;
    private Integer overallScore;
    private List<RiskItem> items;

    public RiskRadarResponse(Integer days, String overallLevel, Integer overallScore, List<RiskItem> items) {
        this.days = days;
        this.overallLevel = overallLevel;
        this.overallScore = overallScore;
        this.items = items;
    }

    public Integer getDays() { return days; }
    public String getOverallLevel() { return overallLevel; }
    public Integer getOverallScore() { return overallScore; }
    public List<RiskItem> getItems() { return items; }
}
