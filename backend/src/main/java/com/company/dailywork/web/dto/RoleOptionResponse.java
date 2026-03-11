package com.company.dailywork.web.dto;

public class RoleOptionResponse {
    private String code;
    private String label;
    private String description;

    public RoleOptionResponse(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
