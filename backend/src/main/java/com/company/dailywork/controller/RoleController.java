package com.company.dailywork.controller;

import com.company.dailywork.common.model.ApiResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.dailywork.entity.Role;
import com.company.dailywork.mapper.RoleMapper;
import com.company.dailywork.web.dto.RoleOptionResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class RoleController {

    private final RoleMapper roleMapper;

    public RoleController(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @GetMapping
    public ApiResponse<List<RoleOptionResponse>> listRoles() {
        List<RoleOptionResponse> roles = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                        .eq(Role::getStatus, 1)
                        .orderByAsc(Role::getId))
                .stream()
                .map(role -> new RoleOptionResponse(role.getCode(), role.getLabel(), role.getDescription()))
                .toList();
        return ApiResponse.success(roles);
    }
}
