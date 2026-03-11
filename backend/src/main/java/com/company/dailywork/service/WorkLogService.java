package com.company.dailywork.service;

import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.web.dto.WorkLogResponse;
import com.company.dailywork.web.dto.WorkLogSaveRequest;
import com.company.dailywork.web.dto.WorkLogStatisticsResponse;

import java.time.LocalDate;
import java.util.List;

public interface WorkLogService {
    WorkLogResponse saveOrUpdate(WorkLogSaveRequest request, SecurityUser user);
    WorkLogResponse getById(Long id, SecurityUser user);
    List<WorkLogResponse> list(SecurityUser user, LocalDate startDate, LocalDate endDate);
    void delete(Long id, SecurityUser user);
    WorkLogStatisticsResponse statistics(SecurityUser user, String period);
    String exportCsv(SecurityUser user, LocalDate startDate, LocalDate endDate);
}
