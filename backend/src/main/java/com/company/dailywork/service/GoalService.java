package com.company.dailywork.service;

import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.web.dto.GoalBoardResponse;
import com.company.dailywork.web.dto.SprintGoalResponse;
import com.company.dailywork.web.dto.SprintGoalSaveRequest;

import java.util.List;

public interface GoalService {
    SprintGoalResponse saveOrUpdate(SprintGoalSaveRequest request, SecurityUser user);
    List<SprintGoalResponse> list(SecurityUser user);
    void delete(Long id, SecurityUser user);
    GoalBoardResponse board(SecurityUser user, Integer days);
}
