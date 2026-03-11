package com.company.dailywork.web.dto;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String username;
    private String nickname;
    private String employedCompany;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public UserResponse(Long id, String username, String nickname, String employedCompany, String role, Integer status,
                        LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.employedCompany = employedCompany;
        this.role = role;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmployedCompany() {
        return employedCompany;
    }

    public String getRole() {
        return role;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
}
