package com.company.dailywork.service;

import com.company.dailywork.entity.User;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.web.dto.CreateUserRequest;
import com.company.dailywork.web.dto.RegisterRequest;
import com.company.dailywork.web.dto.UserResponse;

import java.util.List;

public interface UserService {
    User getByUsername(String username);

    User register(RegisterRequest request);

    User createUser(CreateUserRequest request, SecurityUser operator);

    List<UserResponse> listUsers();

    User updateRole(Long userId, String roleCode, SecurityUser operator);

    User updateStatus(Long userId, Integer status, SecurityUser operator);

    User updateEmployedCompany(Long userId, String employedCompany, SecurityUser operator);

    User updateMyEmployedCompany(SecurityUser user, String employedCompany);

    void changeMyPassword(SecurityUser user, String oldPassword, String newPassword);

    void resetPassword(Long userId, String newPassword, SecurityUser operator);
}
