package com.company.dailywork.web.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileEmploymentRequest {

    @Size(max = 120, message = "length must be <= 120")
    private String employedCompany;

    public String getEmployedCompany() {
        return employedCompany;
    }

    public void setEmployedCompany(String employedCompany) {
        this.employedCompany = employedCompany;
    }
}
