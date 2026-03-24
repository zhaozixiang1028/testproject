package com.company.dailywork.web.dto;

import java.util.List;

public class GoalBoardResponse {
    private Integer days;
    private Integer totalGoals;
    private Integer completedGoals;
    private String avgCompletionRate;
    private List<SprintGoalResponse> goals;
    private List<SprintGoalResponse> highRiskGoals;

    public GoalBoardResponse(Integer days, Integer totalGoals, Integer completedGoals,
                             String avgCompletionRate, List<SprintGoalResponse> goals,
                             List<SprintGoalResponse> highRiskGoals) {
        this.days = days;
        this.totalGoals = totalGoals;
        this.completedGoals = completedGoals;
        this.avgCompletionRate = avgCompletionRate;
        this.goals = goals;
        this.highRiskGoals = highRiskGoals;
    }

    public Integer getDays() { return days; }
    public Integer getTotalGoals() { return totalGoals; }
    public Integer getCompletedGoals() { return completedGoals; }
    public String getAvgCompletionRate() { return avgCompletionRate; }
    public List<SprintGoalResponse> getGoals() { return goals; }
    public List<SprintGoalResponse> getHighRiskGoals() { return highRiskGoals; }
}
