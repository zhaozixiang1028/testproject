package com.company.dailywork.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.dailywork.mapper.model.UserCompanyRecord;
import com.company.dailywork.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

public interface UserCompanyMapper extends BaseMapper<User> {

    @Insert("INSERT INTO sys_user_company(user_id, company_id) VALUES(#{userId}, #{companyId}) ON DUPLICATE KEY UPDATE company_id = VALUES(company_id)")
    int upsert(@Param("userId") Long userId, @Param("companyId") Long companyId);

    @Select("SELECT COALESCE(c.`name`, '') FROM sys_user_company uc LEFT JOIN sys_company c ON uc.company_id = c.id WHERE uc.user_id = #{userId} LIMIT 1")
    String findCompanyNameByUserId(@Param("userId") Long userId);
}
