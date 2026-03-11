package com.company.dailywork.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.dailywork.entity.WorkLog;
import com.company.dailywork.mapper.WorkLogMapper;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.WorkLogService;
import com.company.dailywork.web.dto.WorkLogResponse;
import com.company.dailywork.web.dto.WorkLogSaveRequest;
import com.company.dailywork.web.dto.WorkLogStatisticsResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkLogServiceImpl implements WorkLogService {

    private final WorkLogMapper workLogMapper;

    public WorkLogServiceImpl(WorkLogMapper workLogMapper) {
        this.workLogMapper = workLogMapper;
    }

    @Override
    public WorkLogResponse saveOrUpdate(WorkLogSaveRequest request, SecurityUser user) {
        WorkLog entity;
        if (request.getId() != null) {
            entity = requireOwned(request.getId(), user.getUser().getId());
        } else {
            entity = new WorkLog();
            entity.setUserId(user.getUser().getId());
        }

        entity.setWorkDate(request.getWorkDate());
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setProjectName(request.getProjectName());
        entity.setTaskType(request.getTaskType());
        entity.setPriorityLevel(request.getPriorityLevel());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setTags(request.getTags());
        entity.setAttachmentUrls(request.getAttachmentUrls());
        entity.setWorkHours(calcHours(request));

        if (request.getId() != null) {
            workLogMapper.updateById(entity);
        } else {
            workLogMapper.insert(entity);
        }
        return toResponse(entity);
    }

    @Override
    public WorkLogResponse getById(Long id, SecurityUser user) {
        return toResponse(requireOwned(id, user.getUser().getId()));
    }

    @Override
    public List<WorkLogResponse> list(SecurityUser user, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<WorkLog> query = new LambdaQueryWrapper<WorkLog>()
                .eq(WorkLog::getUserId, user.getUser().getId())
                .orderByDesc(WorkLog::getWorkDate)
                .orderByDesc(WorkLog::getId);
        if (startDate != null) {
            query.ge(WorkLog::getWorkDate, startDate);
        }
        if (endDate != null) {
            query.le(WorkLog::getWorkDate, endDate);
        }
        return workLogMapper.selectList(query).stream().map(this::toResponse).toList();
    }

    @Override
    public void delete(Long id, SecurityUser user) {
        WorkLog entity = requireOwned(id, user.getUser().getId());
        workLogMapper.deleteById(entity.getId());
    }

    @Override
    public WorkLogStatisticsResponse statistics(SecurityUser user, String period) {
        LocalDate end = LocalDate.now();
        LocalDate start;
        if ("day".equalsIgnoreCase(period)) {
            start = end;
        } else if ("month".equalsIgnoreCase(period)) {
            start = end.minusDays(29);
        } else {
            start = end.minusDays(6);
            period = "week";
        }

        List<WorkLog> logs = workLogMapper.selectList(new LambdaQueryWrapper<WorkLog>()
                .eq(WorkLog::getUserId, user.getUser().getId())
                .between(WorkLog::getWorkDate, start, end));

        BigDecimal total = logs.stream()
                .map(log -> log.getWorkHours() == null ? BigDecimal.ZERO : log.getWorkHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> projectMap = logs.stream()
                .collect(Collectors.groupingBy(log -> {
                    String name = log.getProjectName();
                    return name == null || name.isBlank() ? "未分类" : name;
                }, Collectors.reducing(BigDecimal.ZERO,
                        log -> log.getWorkHours() == null ? BigDecimal.ZERO : log.getWorkHours(),
                        BigDecimal::add)));

        List<WorkLogStatisticsResponse.ProjectHoursItem> projectHours = projectMap.entrySet().stream()
                .map(entry -> new WorkLogStatisticsResponse.ProjectHoursItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(WorkLogStatisticsResponse.ProjectHoursItem::getHours).reversed())
                .toList();

        return new WorkLogStatisticsResponse(period, total, logs.size(), projectHours);
    }

    @Override
    public String exportCsv(SecurityUser user, LocalDate startDate, LocalDate endDate) {
        List<WorkLogResponse> list = list(user, startDate, endDate);
        StringBuilder sb = new StringBuilder();
        sb.append("id,date,title,project,taskType,priority,workHours,tags\n");
        for (WorkLogResponse row : list) {
            sb.append(row.getId()).append(',')
                    .append(escape(row.getWorkDate() == null ? "" : row.getWorkDate().toString())).append(',')
                    .append(escape(row.getTitle())).append(',')
                    .append(escape(row.getProjectName())).append(',')
                    .append(escape(row.getTaskType())).append(',')
                    .append(escape(row.getPriorityLevel())).append(',')
                    .append(row.getWorkHours() == null ? "0" : row.getWorkHours()).append(',')
                    .append(escape(row.getTags()))
                    .append('\n');
        }
        return sb.toString();
    }

    private String escape(String raw) {
        String v = raw == null ? "" : raw.replace("\"", "\"\"");
        return "\"" + v + "\"";
    }

    private BigDecimal calcHours(WorkLogSaveRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            return BigDecimal.ZERO;
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be later than start time");
        }
        long minutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private WorkLog requireOwned(Long id, Long userId) {
        WorkLog entity = workLogMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("Work log not found");
        }
        if (!userId.equals(entity.getUserId())) {
            throw new IllegalArgumentException("No permission to access this work log");
        }
        return entity;
    }

    private WorkLogResponse toResponse(WorkLog e) {
        return new WorkLogResponse(
                e.getId(), e.getUserId(), e.getWorkDate(), e.getTitle(), e.getContent(), e.getProjectName(),
                e.getTaskType(), e.getPriorityLevel(), e.getStartTime(), e.getEndTime(), e.getWorkHours(),
                e.getTags(), e.getAttachmentUrls(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
