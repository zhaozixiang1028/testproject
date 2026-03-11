package com.company.dailywork.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.dailywork.entity.CompanyCatalog;
import com.company.dailywork.entity.Role;
import com.company.dailywork.entity.User;
import com.company.dailywork.mapper.CompanyCatalogMapper;
import com.company.dailywork.mapper.RoleMapper;
import com.company.dailywork.mapper.UserCompanyMapper;
import com.company.dailywork.mapper.UserRoleMapper;
import com.company.dailywork.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final UserCompanyMapper userCompanyMapper;
    private final RoleMapper roleMapper;
    private final CompanyCatalogMapper companyCatalogMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper,
                           UserCompanyMapper userCompanyMapper,
                           RoleMapper roleMapper,
                           CompanyCatalogMapper companyCatalogMapper,
                           UserRoleMapper userRoleMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userCompanyMapper = userCompanyMapper;
        this.roleMapper = roleMapper;
        this.companyCatalogMapper = companyCatalogMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureUser("superadmin", "Super Admin", null, "SUPER_ADMIN", "Admin@123");
        ensureUser("admin", "Admin", null, "ADMIN", "Admin@123");
        ensureUser("employee", "Employee", "TestCompany", "EMPLOYEE", "Emp@12345");
    }

    private void ensureUser(String username, String nickname, String employedCompany, String role, String rawPassword) {
        User existed = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username).last("limit 1"));
        if (existed != null) {
            syncEmployment(existed, existed.getEmployedCompany());
            bindRole(existed.getId(), role);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);
        user.setEmployedCompany(employedCompany);
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);
        syncEmployment(user, employedCompany);
        bindRole(user.getId(), role);
    }

    private void syncEmployment(User user, String employedCompany) {
        Long companyId = resolveCompanyId(employedCompany);
        userCompanyMapper.upsert(user.getId(), companyId);
    }

    private Long resolveCompanyId(String companyName) {
        String normalized = normalizeCompany(companyName);
        if (normalized == null) {
            return 0L;
        }

        CompanyCatalog existed = companyCatalogMapper.selectOne(new LambdaQueryWrapper<CompanyCatalog>()
                .eq(CompanyCatalog::getName, normalized)
                .last("limit 1"));
        if (existed != null) {
            return existed.getId();
        }

        CompanyCatalog entity = new CompanyCatalog();
        entity.setName(normalized);
        entity.setStatus(1);
        companyCatalogMapper.insert(entity);
        return entity.getId();
    }

    private String normalizeCompany(String companyName) {
        if (companyName == null) {
            return null;
        }
        String normalized = companyName.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void bindRole(Long userId, String roleCode) {
        Long roleId = userRoleMapper.findRoleIdByCode(roleCode);
        if (roleId == null) {
                Long count = roleMapper.selectCount(new LambdaQueryWrapper<Role>()
                    .eq(Role::getCode, roleCode));
            if (count == null || count == 0) {
                return;
            }
            roleId = userRoleMapper.findRoleIdByCode(roleCode);
        }
        if (roleId == null) {
            return;
        }

        userRoleMapper.deleteByUserId(userId);
        userRoleMapper.insertUserRole(userId, roleId);
    }
}
