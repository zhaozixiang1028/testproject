package com.company.dailywork.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.dailywork.entity.SprintGoal;
import com.company.dailywork.entity.WorkLog;
import com.company.dailywork.mapper.SprintGoalMapper;
import com.company.dailywork.mapper.WorkLogMapper;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.GoalService;
import com.company.dailywork.web.dto.GoalBoardResponse;
import com.company.dailywork.web.dto.SprintGoalResponse;
import com.company.dailywork.web.dto.SprintGoalSaveRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class GoalServiceImpl implements GoalService {

    private final SprintGoalMapper sprintGoalMapper;
    private final WorkLogMapper workLogMapper;

    public GoalServiceImpl(SprintGoalMapper sprintGoalMapper, WorkLogMapper workLogMapper) {
        this.sprintGoalMapper = sprintGoalMapper;
        this.workLogMapper = workLogMapper;
    }

    @Override
    public SprintGoalResponse saveOrUpdate(SprintGoalSaveRequest request, SecurityUser user) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be later than start date");
        }

        SprintGoal entity;
        if (request.getId() != null) {
            entity = requireOwned(request.getId(), user.getUser().getId());
        } else {
            entity = new SprintGoal();
            entity.setUserId(user.getUser().getId());
        }

        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setRelatedProject(request.getRelatedProject());
        entity.setTargetHours(request.getTargetHours());
        entity.setStatus(normalizeStatus(request.getStatus()));
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());

        if (request.getId() != null) {
            sprintGoalMapper.updateById(entity);
        } else {
            sprintGoalMapper.insert(entity);
        }

        return toResponse(entity);
    }

    @Override
    public List<SprintGoalResponse> list(SecurityUser user) {
        return sprintGoalMapper.selectList(new LambdaQueryWrapper<SprintGoal>()
                        .eq(SprintGoal::getUserId, user.getUser().getId())
                        .orderByDesc(SprintGoal::getCreatedAt)
                        .orderByDesc(SprintGoal::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id, SecurityUser user) {
        SprintGoal goal = requireOwned(id, user.getUser().getId());
        sprintGoalMapper.deleteById(goal.getId());
    }

    @Override
    public GoalBoardResponse board(SecurityUser user, Integer days) {
        int validDays = (days == null || days < 7) ? 14 : Math.min(days, 90);
        LocalDate start = LocalDate.now().minusDays(validDays - 1L);

        List<SprintGoalResponse> goals = list(user);
        List<SprintGoalResponse> filtered = goals.stream()
                .filter(goal -> goal.getEndDate() == null || !goal.getEndDate().isBefore(start))
                .toList();

        int completed = (int) filtered.stream().filter(goal -> "DONE".equals(goal.getStatus())).count();

        BigDecimal avgRate = filtered.isEmpty()
                ? BigDecimal.ZERO
                : filtered.stream()
                .map(goal -> new BigDecimal(goal.getCompletionRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(filtered.size()), 2, RoundingMode.HALF_UP);

        List<SprintGoalResponse> highRisk = filtered.stream()
                .filter(goal -> {
                    BigDecimal rate = new BigDecimal(goal.getCompletionRate());
                    return "OPEN".equals(goal.getStatus()) && rate.compareTo(BigDecimal.valueOf(45)) < 0;
                })
                .sorted(Comparator.comparing(SprintGoalResponse::getEndDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(5)
                .toList();

        return new GoalBoardResponse(validDays, filtered.size(), completed,
                avgRate.setScale(2, RoundingMode.HALF_UP).toPlainString(), filtered, highRisk);
    }

    private SprintGoal requireOwned(Long id, Long userId) {
        SprintGoal goal = sprintGoalMapper.selectById(id);
        if (goal == null) {
            throw new IllegalArgumentException("Goal not found");
        }
        if (!userId.equals(goal.getUserId())) {
            throw new IllegalArgumentException("No permission to access this goal");
        }
        return goal;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "OPEN";
        }
        String value = status.toUpperCase();
        if (!List.of("OPEN", "DONE", "PAUSED").contains(value)) {
            return "OPEN";
        }
        return value;
    }

    private SprintGoalResponse toResponse(SprintGoal goal) {
        BigDecimal doneHours = calculateCompletedHours(goal);
        BigDecimal target = goal.getTargetHours() == null || goal.getTargetHours().compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ONE
                : goal.getTargetHours();

        BigDecimal completionRate = doneHours.multiply(BigDecimal.valueOf(100))
                .divide(target, 2, RoundingMode.HALF_UP);

        return new SprintGoalResponse(
                goal.getId(),
                goal.getUserId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getRelatedProject(),
                target.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                doneHours.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                completionRate.toPlainString(),
                normalizeStatus(goal.getStatus()),
                goal.getStartDate(),
                goal.getEndDate(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }

    private BigDecimal calculateCompletedHours(SprintGoal goal) {
        LocalDate start = goal.getStartDate() == null ? LocalDate.now().minusDays(14) : goal.getStartDate();
        LocalDate end = goal.getEndDate() == null ? LocalDate.now() : goal.getEndDate();

        LambdaQueryWrapper<WorkLog> query = new LambdaQueryWrapper<WorkLog>()
                .eq(WorkLog::getUserId, goal.getUserId())
                .between(WorkLog::getWorkDate, start, end);

        if (goal.getRelatedProject() != null && !goal.getRelatedProject().isBlank()) {
            query.eq(WorkLog::getProjectName, goal.getRelatedProject());
        }

        return workLogMapper.selectList(query).stream()
                .map(item -> item.getWorkHours() == null ? BigDecimal.ZERO : item.getWorkHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
