package com.company.dailywork.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.dailywork.entity.CompanyReview;
import com.company.dailywork.entity.SprintGoal;
import com.company.dailywork.entity.WorkLog;
import com.company.dailywork.mapper.CompanyReviewMapper;
import com.company.dailywork.mapper.SprintGoalMapper;
import com.company.dailywork.mapper.WorkLogMapper;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.InsightsService;
import com.company.dailywork.web.dto.MoodTrendResponse;
import com.company.dailywork.web.dto.RetrospectiveResponse;
import com.company.dailywork.web.dto.RiskRadarResponse;
import com.company.dailywork.web.dto.ReviewAlertResponse;
import com.company.dailywork.web.dto.SkillPortraitResponse;
import com.company.dailywork.web.dto.WeeklyReportResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class InsightsServiceImpl implements InsightsService {

    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^a-zA-Z0-9\u4e00-\u9fa5]+");

    private final WorkLogMapper workLogMapper;
    private final CompanyReviewMapper companyReviewMapper;
    private final SprintGoalMapper sprintGoalMapper;

    public InsightsServiceImpl(WorkLogMapper workLogMapper,
                               CompanyReviewMapper companyReviewMapper,
                               SprintGoalMapper sprintGoalMapper) {
        this.workLogMapper = workLogMapper;
        this.companyReviewMapper = companyReviewMapper;
        this.sprintGoalMapper = sprintGoalMapper;
    }

    @Override
    public WeeklyReportResponse weeklyReport(SecurityUser user, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        String normalizedPeriod;
        if ("month".equalsIgnoreCase(period)) {
            startDate = endDate.minusDays(29);
            normalizedPeriod = "month";
        } else {
            startDate = endDate.minusDays(6);
            normalizedPeriod = "week";
        }

        List<WorkLog> logs = workLogMapper.selectList(new LambdaQueryWrapper<WorkLog>()
                .eq(WorkLog::getUserId, user.getUser().getId())
                .between(WorkLog::getWorkDate, startDate, endDate)
                .orderByDesc(WorkLog::getWorkDate));

        BigDecimal totalHours = logs.stream()
                .map(log -> log.getWorkHours() == null ? BigDecimal.ZERO : log.getWorkHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String summary = String.format(Locale.ROOT,
                "%s内共记录%d条日志，总工时%s小时，项目推进节奏%s。",
                "week".equals(normalizedPeriod) ? "本周" : "本月",
                logs.size(),
                totalHours.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                totalHours.compareTo(BigDecimal.valueOf(35)) >= 0 ? "较高" : "平稳");

        List<String> highlights = buildHighlights(logs);
        List<String> risks = buildRisks(logs, totalHours);
        List<String> nextPlans = buildNextPlans(logs);

        return new WeeklyReportResponse(
                normalizedPeriod,
                startDate,
                endDate,
                logs.size(),
                totalHours.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                summary,
                highlights,
                risks,
                nextPlans
        );
    }

    @Override
    public MoodTrendResponse moodTrend(SecurityUser user, Integer days) {
        int validDays = (days == null || days < 7) ? 14 : Math.min(days, 60);
        LocalDate startDate = LocalDate.now().minusDays(validDays - 1L);

        List<WorkLog> logs = workLogMapper.selectList(new LambdaQueryWrapper<WorkLog>()
                .eq(WorkLog::getUserId, user.getUser().getId())
                .ge(WorkLog::getWorkDate, startDate)
                .isNotNull(WorkLog::getMoodScore)
                .orderByAsc(WorkLog::getWorkDate));

        List<MoodTrendResponse.MoodPoint> series = logs.stream()
                .map(log -> new MoodTrendResponse.MoodPoint(log.getWorkDate().toString(), log.getMoodScore()))
                .toList();

        BigDecimal avgMood = logs.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(logs.stream().mapToInt(log -> log.getMoodScore() == null ? 0 : log.getMoodScore()).average().orElse(0));

        MoodTrendResponse.MoodPoint lowest = logs.stream()
                .min(Comparator.comparingInt(log -> log.getMoodScore() == null ? 6 : log.getMoodScore()))
                .map(log -> new MoodTrendResponse.MoodPoint(log.getWorkDate().toString(), log.getMoodScore()))
                .orElse(null);

        List<String> advice = new ArrayList<>();
        if (logs.isEmpty()) {
            advice.add("先在日志里记录每日情绪分(1-5)，系统会自动生成趋势建议。");
        } else if (avgMood.doubleValue() < 2.8) {
            advice.add("近期情绪偏低，建议优先梳理高压任务并和主管沟通资源支持。");
            advice.add("尝试把连续复杂任务拆分，给自己保留可完成的小目标。\n");
        } else if (avgMood.doubleValue() < 3.6) {
            advice.add("情绪处于中位，建议保证睡眠并减少多任务并行带来的切换成本。");
        } else {
            advice.add("状态整体不错，建议继续保持稳定节奏并复盘高效工作时段。");
        }

        return new MoodTrendResponse(
                validDays,
                avgMood.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                lowest,
                series,
                advice
        );
    }

    @Override
    public SkillPortraitResponse skillPortrait(SecurityUser user, Integer days) {
        int validDays = (days == null || days < 14) ? 30 : Math.min(days, 120);
        LocalDate startDate = LocalDate.now().minusDays(validDays - 1L);

        List<WorkLog> logs = workLogMapper.selectList(new LambdaQueryWrapper<WorkLog>()
                .eq(WorkLog::getUserId, user.getUser().getId())
                .ge(WorkLog::getWorkDate, startDate));

        Set<String> stopWords = Set.of("今天", "完成", "处理", "推进", "任务", "项目", "工作", "问题", "需求", "沟通", "会议", "测试", "开发", "优化", "the", "and");
        Map<String, Integer> mentionCount = new HashMap<>();
        Map<String, BigDecimal> relatedHours = new HashMap<>();

        for (WorkLog log : logs) {
            String text = (safe(log.getTitle()) + " " + safe(log.getContent()) + " " + safe(log.getTags()) + " "
                    + safe(log.getProjectName()) + " " + safe(log.getTaskType())).toLowerCase(Locale.ROOT);
            String[] tokens = TOKEN_SPLIT.split(text);
            Set<String> seenInLog = new HashSet<>();
            for (String token : tokens) {
                String t = token.trim();
                if (t.length() < 2 || stopWords.contains(t)) {
                    continue;
                }
                if (seenInLog.add(t)) {
                    mentionCount.put(t, mentionCount.getOrDefault(t, 0) + 1);
                    relatedHours.put(t, relatedHours.getOrDefault(t, BigDecimal.ZERO)
                            .add(log.getWorkHours() == null ? BigDecimal.ZERO : log.getWorkHours()));
                }
            }
        }

        List<SkillPortraitResponse.SkillItem> topSkills = mentionCount.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(8)
                .map(e -> new SkillPortraitResponse.SkillItem(
                        e.getKey(),
                        e.getValue(),
                        relatedHours.getOrDefault(e.getKey(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).toPlainString()))
                .toList();

        List<String> suggestions = new ArrayList<>();
        if (topSkills.isEmpty()) {
            suggestions.add("日志中的技能关键词较少，建议在标题或标签中明确技术名词与业务能力点。");
        } else {
            suggestions.add("当前高频技能是「" + topSkills.get(0).getSkill() + "」，建议沉淀模板或文档扩大复用价值。");
            if (topSkills.size() > 1) {
                suggestions.add("可将「" + topSkills.get(1).getSkill() + "」作为下阶段重点，结合一个真实项目做深度练习。");
            }
            suggestions.add("建议每周至少补充1条“复盘型日志”，强化可迁移能力总结。\n");
        }

        return new SkillPortraitResponse(validDays, topSkills, suggestions);
    }

    @Override
    public ReviewAlertResponse reviewAlerts(Integer days) {
        int validDays = (days == null || days < 7) ? 14 : Math.min(days, 60);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recentStart = now.minusDays(validDays);
        LocalDateTime previousStart = recentStart.minusDays(validDays);

        List<CompanyReview> recent = companyReviewMapper.selectList(new LambdaQueryWrapper<CompanyReview>()
                .ge(CompanyReview::getCreatedAt, recentStart)
                .le(CompanyReview::getCreatedAt, now));

        List<CompanyReview> previous = companyReviewMapper.selectList(new LambdaQueryWrapper<CompanyReview>()
                .ge(CompanyReview::getCreatedAt, previousStart)
                .lt(CompanyReview::getCreatedAt, recentStart));

        Map<String, List<CompanyReview>> recentByCompany = recent.stream().collect(java.util.stream.Collectors.groupingBy(CompanyReview::getCompanyName));
        Map<String, List<CompanyReview>> previousByCompany = previous.stream().collect(java.util.stream.Collectors.groupingBy(CompanyReview::getCompanyName));

        List<ReviewAlertResponse.AlertItem> warnings = new ArrayList<>();
        for (Map.Entry<String, List<CompanyReview>> entry : recentByCompany.entrySet()) {
            String company = entry.getKey();
            List<CompanyReview> recentList = entry.getValue();
            if (recentList.size() < 2) {
                continue;
            }

            List<CompanyReview> previousList = previousByCompany.getOrDefault(company, List.of());
            double currentAvg = averageScore(recentList);
            double previousAvg = previousList.isEmpty() ? currentAvg : averageScore(previousList);
            double dropRate = previousAvg <= 0 ? 0 : ((previousAvg - currentAvg) / previousAvg) * 100;
            long negativeCount = recentList.stream().filter(item -> averageScore(item) <= 2.6).count();
            double negativeRate = recentList.isEmpty() ? 0 : (negativeCount * 100.0 / recentList.size());

            if (dropRate >= 12 || negativeRate >= 45) {
                String level = (dropRate >= 20 || negativeRate >= 60) ? "HIGH" : "MEDIUM";
                warnings.add(new ReviewAlertResponse.AlertItem(
                        company,
                        recentList.size(),
                        toFixed(currentAvg),
                        toFixed(previousAvg),
                        toFixed(dropRate),
                        toFixed(negativeRate),
                        level
                ));
            }
        }

        warnings.sort((a, b) -> b.getLevel().compareTo(a.getLevel()));
        return new ReviewAlertResponse(validDays, warnings.size(), warnings);
    }

    @Override
    public RiskRadarResponse riskRadar(SecurityUser user, Integer days) {
        int validDays = (days == null || days < 7) ? 14 : Math.min(days, 90);
        LocalDate startDate = LocalDate.now().minusDays(validDays - 1L);

        List<WorkLog> logs = workLogMapper.selectList(new LambdaQueryWrapper<WorkLog>()
                .eq(WorkLog::getUserId, user.getUser().getId())
                .ge(WorkLog::getWorkDate, startDate));

        List<SprintGoal> goals = sprintGoalMapper.selectList(new LambdaQueryWrapper<SprintGoal>()
                .eq(SprintGoal::getUserId, user.getUser().getId())
                .ge(SprintGoal::getEndDate, startDate));

        List<RiskRadarResponse.RiskItem> items = new ArrayList<>();
        int score = 100;

        double avgMood = logs.stream()
                .filter(log -> log.getMoodScore() != null)
                .mapToInt(WorkLog::getMoodScore)
                .average()
                .orElse(0);
        if (avgMood > 0 && avgMood < 2.8) {
            score -= 28;
            items.add(new RiskRadarResponse.RiskItem(
                    "MOOD",
                    "HIGH",
                    "情绪低位风险",
                    "近周期情绪分偏低，可能影响执行稳定性。",
                    toFixed(avgMood)));
        } else if (avgMood > 0 && avgMood < 3.5) {
            score -= 14;
            items.add(new RiskRadarResponse.RiskItem(
                    "MOOD",
                    "MEDIUM",
                    "情绪波动关注",
                    "建议控制任务切换频次，降低认知负荷。",
                    toFixed(avgMood)));
        }

        long overtimeDays = logs.stream()
                .filter(log -> log.getWorkHours() != null && log.getWorkHours().compareTo(BigDecimal.valueOf(9)) >= 0)
                .count();
        if (overtimeDays >= 4) {
            score -= 20;
            items.add(new RiskRadarResponse.RiskItem(
                    "WORKLOAD",
                    "HIGH",
                    "工时过载",
                    "连续高工时天数较多，存在疲劳积累。",
                    String.valueOf(overtimeDays)));
        } else if (overtimeDays >= 2) {
            score -= 10;
            items.add(new RiskRadarResponse.RiskItem(
                    "WORKLOAD",
                    "MEDIUM",
                    "工时偏高",
                    "建议提前拆分高优任务并预留缓冲。",
                    String.valueOf(overtimeDays)));
        }

        long blockedGoals = goals.stream()
                .filter(goal -> goal.getStatus() == null || !"DONE".equalsIgnoreCase(goal.getStatus()))
                .filter(goal -> goal.getEndDate() != null && !goal.getEndDate().isAfter(LocalDate.now()))
                .count();
        if (blockedGoals > 0) {
            score -= Math.min(24, (int) blockedGoals * 8);
            items.add(new RiskRadarResponse.RiskItem(
                    "GOAL",
                    blockedGoals >= 2 ? "HIGH" : "MEDIUM",
                    "目标延期风险",
                    "存在到期未完成目标，建议复盘依赖阻塞和资源投入。",
                    String.valueOf(blockedGoals)));
        }

        if ("ADMIN".equalsIgnoreCase(user.getUser().getRole()) || "SUPER_ADMIN".equalsIgnoreCase(user.getUser().getRole())) {
            ReviewAlertResponse reviewWarnings = reviewAlerts(validDays);
            if (reviewWarnings.getTotalWarnings() >= 2) {
                score -= 16;
                items.add(new RiskRadarResponse.RiskItem(
                        "REVIEW",
                        "HIGH",
                        "口碑波动风险",
                        "多家公司出现评价下滑或负面聚集。",
                        String.valueOf(reviewWarnings.getTotalWarnings())));
            } else if (reviewWarnings.getTotalWarnings() == 1) {
                score -= 8;
                items.add(new RiskRadarResponse.RiskItem(
                        "REVIEW",
                        "MEDIUM",
                        "口碑预警",
                        "检测到公司评价异动，建议尽快复核。",
                        String.valueOf(reviewWarnings.getTotalWarnings())));
            }
        }

        score = Math.max(5, score);
        String level = score >= 80 ? "LOW" : (score >= 60 ? "MEDIUM" : "HIGH");
        if (items.isEmpty()) {
            items.add(new RiskRadarResponse.RiskItem(
                    "SYSTEM",
                    "LOW",
                    "整体状态稳定",
                    "当前未发现明显风险，可继续保持节奏。",
                    "-"));
        }

        return new RiskRadarResponse(validDays, level, score, items);
    }

    @Override
    public RetrospectiveResponse retrospective(SecurityUser user, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        String normalizedPeriod;
        if ("month".equalsIgnoreCase(period)) {
            startDate = endDate.minusDays(29);
            normalizedPeriod = "month";
        } else {
            startDate = endDate.minusDays(6);
            normalizedPeriod = "week";
        }

        List<WorkLog> logs = workLogMapper.selectList(new LambdaQueryWrapper<WorkLog>()
                .eq(WorkLog::getUserId, user.getUser().getId())
                .between(WorkLog::getWorkDate, startDate, endDate));

        List<SprintGoal> goals = sprintGoalMapper.selectList(new LambdaQueryWrapper<SprintGoal>()
                .eq(SprintGoal::getUserId, user.getUser().getId())
                .le(SprintGoal::getStartDate, endDate)
                .ge(SprintGoal::getEndDate, startDate));

        BigDecimal totalHours = logs.stream()
                .map(log -> log.getWorkHours() == null ? BigDecimal.ZERO : log.getWorkHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> projectHours = new HashMap<>();
        for (WorkLog log : logs) {
            String project = blankToDefault(log.getProjectName(), "未分类项目");
            projectHours.put(project, projectHours.getOrDefault(project, BigDecimal.ZERO)
                    .add(log.getWorkHours() == null ? BigDecimal.ZERO : log.getWorkHours()));
        }

        String theme = projectHours.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("执行节奏优化");

        String summary = String.format(Locale.ROOT,
                "%s复盘共覆盖%d条日志，累计工时%s小时，聚焦主题为「%s」。",
                "week".equals(normalizedPeriod) ? "本周" : "本月",
                logs.size(),
                totalHours.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                theme);

        List<String> whatWentWell = new ArrayList<>();
        if (!projectHours.isEmpty()) {
            whatWentWell.add("在「" + theme + "」上的投入最高，执行连续性较好。");
        }
        long doneGoals = goals.stream().filter(goal -> "DONE".equalsIgnoreCase(blankToDefault(goal.getStatus(), "OPEN"))).count();
        if (doneGoals > 0) {
            whatWentWell.add("周期内完成目标数为" + doneGoals + "，目标闭环能力提升。");
        }
        if (whatWentWell.isEmpty()) {
            whatWentWell.add("建议先建立最小可衡量成果，提升复盘可见性。");
        }

        List<String> toImprove = new ArrayList<>();
        long highPriorityCount = logs.stream().filter(log -> "HIGH".equalsIgnoreCase(blankToDefault(log.getPriorityLevel(), ""))).count();
        if (highPriorityCount >= 4) {
            toImprove.add("高优任务占比偏高，可提前规划依赖关系并降低临时插单。");
        }
        long pendingGoals = goals.stream().filter(goal -> !"DONE".equalsIgnoreCase(blankToDefault(goal.getStatus(), "OPEN"))).count();
        if (pendingGoals > 0) {
            toImprove.add("仍有" + pendingGoals + "个目标未完成，建议明确阻塞点与负责人。");
        }
        if (toImprove.isEmpty()) {
            toImprove.add("整体节奏稳定，可增强文档沉淀与知识复用效率。");
        }

        List<String> nextActions = List.of(
                "下周期首日明确3个最重要目标并写入目标看板。",
                "每两天执行一次风险雷达检查，及时调整任务优先级。",
                "周期结束前导出复盘模板并补齐成果证据。"
        );

        return new RetrospectiveResponse(
                normalizedPeriod,
                startDate,
                endDate,
                theme,
                summary,
                whatWentWell,
                toImprove,
                nextActions
        );
    }

    private List<String> buildHighlights(List<WorkLog> logs) {
        if (logs.isEmpty()) {
            return List.of("本周期暂无日志记录，可先补充关键任务进展。");
        }
        Map<String, BigDecimal> projectHours = new HashMap<>();
        for (WorkLog log : logs) {
            String project = blankToDefault(log.getProjectName(), "未分类项目");
            projectHours.put(project, projectHours.getOrDefault(project, BigDecimal.ZERO)
                    .add(log.getWorkHours() == null ? BigDecimal.ZERO : log.getWorkHours()));
        }

        String topProject = projectHours.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("未分类项目");

        return List.of(
                "投入最多的项目是「" + topProject + "」。",
                "建议将本周期的关键成果整理为2-3条可复用经验。"
        );
    }

    private List<String> buildRisks(List<WorkLog> logs, BigDecimal totalHours) {
        List<String> risks = new ArrayList<>();
        if (totalHours.compareTo(BigDecimal.valueOf(55)) > 0) {
            risks.add("总工时偏高，存在持续疲劳风险，建议下周期预留缓冲时间。");
        }
        long highPriorityCount = logs.stream()
                .filter(log -> "HIGH".equalsIgnoreCase(blankToDefault(log.getPriorityLevel(), "")))
                .count();
        if (highPriorityCount >= 4) {
            risks.add("高优先级任务数量较多，建议提前进行依赖排期，降低突发阻塞。\n");
        }
        if (risks.isEmpty()) {
            risks.add("暂无明显风险，保持当前节奏并关注跨团队协作依赖。\n");
        }
        return risks;
    }

    private List<String> buildNextPlans(List<WorkLog> logs) {
        if (logs.isEmpty()) {
            return List.of("下周期建议先制定3个可量化小目标，再按优先级推进。");
        }

        Set<String> projects = logs.stream()
                .map(log -> blankToDefault(log.getProjectName(), "未分类项目"))
                .limit(3)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        List<String> plans = new ArrayList<>();
        plans.add("继续推进项目：「" + String.join("、", projects) + "」。");
        plans.add("每2天完成一次进度复盘，及时暴露风险并同步资源需求。");
        plans.add("至少沉淀1条过程模板，提升后续任务复用效率。");
        return plans;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String blankToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private double averageScore(List<CompanyReview> reviews) {
        return reviews.stream().mapToDouble(this::averageScore).average().orElse(0);
    }

    private double averageScore(CompanyReview review) {
        return (safeInt(review.getCultureScore())
                + safeInt(review.getTeamScore())
                + safeInt(review.getGrowthScore())
                + safeInt(review.getSalaryScore())
                + safeInt(review.getBalanceScore())) / 5.0;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String toFixed(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
