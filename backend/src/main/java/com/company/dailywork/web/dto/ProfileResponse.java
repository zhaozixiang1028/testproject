package com.company.dailywork.web.dto;

public class ProfileResponse {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private String employedCompany;

    public ProfileResponse(Long id, String username, String nickname, String role, String employedCompany) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
        this.employedCompany = employedCompany;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmployedCompany() {
        return employedCompany;
    }

    public void setEmployedCompany(String employedCompany) {
        this.employedCompany = employedCompany;
    }
}
