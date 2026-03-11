package com.company.dailywork.controller;

import com.company.dailywork.common.model.ApiResponse;
import com.company.dailywork.service.CompanyDirectoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyDirectoryController {

    private final CompanyDirectoryService companyDirectoryService;

    public CompanyDirectoryController(CompanyDirectoryService companyDirectoryService) {
        this.companyDirectoryService = companyDirectoryService;
    }

    @GetMapping("/options")
    public ApiResponse<List<String>> options() {
        return ApiResponse.success(companyDirectoryService.listCompanyOptions());
    }
}
