package com.company.dailywork.web.dto;

import java.util.List;

public class MoodTrendResponse {

    public static class MoodPoint {
        private String date;
        private Integer moodScore;

        public MoodPoint(String date, Integer moodScore) {
            this.date = date;
            this.moodScore = moodScore;
        }

        public String getDate() { return date; }
        public Integer getMoodScore() { return moodScore; }
    }

    private Integer days;
    private String avgMood;
    private MoodPoint lowestMoodDay;
    private List<MoodPoint> series;
    private List<String> advice;

    public MoodTrendResponse(Integer days, String avgMood, MoodPoint lowestMoodDay,
                             List<MoodPoint> series, List<String> advice) {
        this.days = days;
        this.avgMood = avgMood;
        this.lowestMoodDay = lowestMoodDay;
        this.series = series;
        this.advice = advice;
    }

    public Integer getDays() { return days; }
    public String getAvgMood() { return avgMood; }
    public MoodPoint getLowestMoodDay() { return lowestMoodDay; }
    public List<MoodPoint> getSeries() { return series; }
    public List<String> getAdvice() { return advice; }
}
