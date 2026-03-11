package com.company.dailywork.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.dailywork.entity.CompanyCatalog;
import com.company.dailywork.entity.CompanyReview;
import com.company.dailywork.mapper.CompanyCatalogMapper;
import com.company.dailywork.mapper.CompanyReviewMapper;
import com.company.dailywork.service.CompanyDirectoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CompanyDirectoryServiceImpl implements CompanyDirectoryService {

    private final CompanyCatalogMapper companyCatalogMapper;
    private final CompanyReviewMapper companyReviewMapper;

    public CompanyDirectoryServiceImpl(CompanyCatalogMapper companyCatalogMapper,
                                       CompanyReviewMapper companyReviewMapper) {
        this.companyCatalogMapper = companyCatalogMapper;
        this.companyReviewMapper = companyReviewMapper;
    }

    @Override
    public List<String> listCompanyOptions() {
        Set<String> merged = new LinkedHashSet<>();

        List<Object> companyNames = companyCatalogMapper.selectObjs(new QueryWrapper<CompanyCatalog>()
                .select("name")
                .eq("status", 1)
                .isNotNull("name")
                .ne("name", "")
                .groupBy("name"));

        mergeNames(merged, companyNames);

        List<Object> reviewCompanies = companyReviewMapper.selectObjs(new QueryWrapper<CompanyReview>()
                .select("company_name")
                .isNotNull("company_name")
                .ne("company_name", "")
                .groupBy("company_name"));

        mergeNames(merged, reviewCompanies);

        List<String> result = new ArrayList<>(merged);
        result.sort(String::compareToIgnoreCase);
        return result;
    }

    private void mergeNames(Set<String> merged, List<Object> values) {
        for (Object item : values) {
            if (item == null) {
                continue;
            }
            String name = item.toString().trim();
            if (!name.isEmpty()) {
                merged.add(name);
            }
        }
    }
}
