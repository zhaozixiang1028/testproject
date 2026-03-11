package com.company.dailywork.controller;

import com.company.dailywork.common.model.ApiResponse;
import com.company.dailywork.entity.User;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.UserService;
import com.company.dailywork.web.dto.CreateUserRequest;
import com.company.dailywork.web.dto.ResetPasswordRequest;
import com.company.dailywork.web.dto.UpdateUserRoleRequest;
import com.company.dailywork.web.dto.UpdateUserStatusRequest;
import com.company.dailywork.web.dto.UpdateUserEmploymentRequest;
import com.company.dailywork.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> listUsers() {
        return ApiResponse.success(userService.listUsers());
    }

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request,
                                                @AuthenticationPrincipal SecurityUser operator) {
        User user = userService.createUser(request, operator);
        return ApiResponse.success("User created", toResponse(user));
    }

    @PutMapping("/{id}/role")
    public ApiResponse<UserResponse> updateRole(@PathVariable Long id,
                                                @RequestBody @Valid UpdateUserRoleRequest request,
                                                @AuthenticationPrincipal SecurityUser operator) {
        User user = userService.updateRole(id, request.getRole(), operator);
        return ApiResponse.success("Role updated", toResponse(user));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<UserResponse> updateStatus(@PathVariable Long id,
                                                  @RequestBody @Valid UpdateUserStatusRequest request,
                                                  @AuthenticationPrincipal SecurityUser operator) {
        User user = userService.updateStatus(id, request.getStatus(), operator);
        return ApiResponse.success("Status updated", toResponse(user));
    }

    @PutMapping("/{id}/employment")
    public ApiResponse<UserResponse> updateEmployment(@PathVariable Long id,
                                                      @RequestBody @Valid UpdateUserEmploymentRequest request,
                                                      @AuthenticationPrincipal SecurityUser operator) {
        User user = userService.updateEmployedCompany(id, request.getEmployedCompany(), operator);
        return ApiResponse.success("Employment updated", toResponse(user));
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @RequestBody @Valid ResetPasswordRequest request,
                                           @AuthenticationPrincipal SecurityUser operator) {
        userService.resetPassword(id, request.getNewPassword(), operator);
        return ApiResponse.success("Password reset", null);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
            user.getEmployedCompany(),
                user.getRole(),
                user.getStatus(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }
}
