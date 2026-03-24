package com.company.dailywork.web.dto;

import java.util.List;

public class SkillPortraitResponse {

    public static class SkillItem {
        private String skill;
        private Integer mentionCount;
        private String relatedHours;

        public SkillItem(String skill, Integer mentionCount, String relatedHours) {
            this.skill = skill;
            this.mentionCount = mentionCount;
            this.relatedHours = relatedHours;
        }

        public String getSkill() { return skill; }
        public Integer getMentionCount() { return mentionCount; }
        public String getRelatedHours() { return relatedHours; }
    }

    private Integer days;
    private List<SkillItem> topSkills;
    private List<String> growthSuggestions;

    public SkillPortraitResponse(Integer days, List<SkillItem> topSkills, List<String> growthSuggestions) {
        this.days = days;
        this.topSkills = topSkills;
        this.growthSuggestions = growthSuggestions;
    }

    public Integer getDays() { return days; }
    public List<SkillItem> getTopSkills() { return topSkills; }
    public List<String> getGrowthSuggestions() { return growthSuggestions; }
}
