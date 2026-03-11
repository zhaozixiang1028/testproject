package com.company.dailywork.common.model;

import java.util.Arrays;
import java.util.List;

public enum UserRole {
    SUPER_ADMIN("超级管理员", "仅开发人员使用，拥有全量权限"),
    ADMIN("管理员", "可管理员工账号和业务数据"),
    EMPLOYEE("员工", "可使用工作日志和评价功能");

    private final String label;
    private final String description;

    UserRole(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + code));
    }

    public static List<UserRole> assignableRolesBy(UserRole operatorRole) {
        if (operatorRole == SUPER_ADMIN) {
            return List.of(SUPER_ADMIN, ADMIN, EMPLOYEE);
        }
        if (operatorRole == ADMIN) {
            return List.of(SUPER_ADMIN, ADMIN, EMPLOYEE);
        }
        return List.of();
    }
}
