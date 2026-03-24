package com.company.dailywork.controller;

import com.company.dailywork.common.model.ApiResponse;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.GoalService;
import com.company.dailywork.web.dto.GoalBoardResponse;
import com.company.dailywork.web.dto.SprintGoalResponse;
import com.company.dailywork.web.dto.SprintGoalSaveRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ApiResponse<SprintGoalResponse> save(@RequestBody @Valid SprintGoalSaveRequest request,
                                                @AuthenticationPrincipal SecurityUser user) {
        return ApiResponse.success("Saved", goalService.saveOrUpdate(request, user));
    }

    @GetMapping
    public ApiResponse<List<SprintGoalResponse>> list(@AuthenticationPrincipal SecurityUser user) {
        return ApiResponse.success(goalService.list(user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal SecurityUser user) {
        goalService.delete(id, user);
        return ApiResponse.success("Deleted", null);
    }

    @GetMapping("/board")
    public ApiResponse<GoalBoardResponse> board(@AuthenticationPrincipal SecurityUser user,
                                                @RequestParam(defaultValue = "14") Integer days) {
        return ApiResponse.success(goalService.board(user, days));
    }
}
