package com.company.dailywork.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.dailywork.common.model.UserRole;
import com.company.dailywork.entity.CompanyCatalog;
import com.company.dailywork.entity.Role;
import com.company.dailywork.entity.User;
import com.company.dailywork.mapper.CompanyCatalogMapper;
import com.company.dailywork.mapper.RoleMapper;
import com.company.dailywork.mapper.UserCompanyMapper;
import com.company.dailywork.mapper.UserRoleMapper;
import com.company.dailywork.mapper.UserMapper;
import com.company.dailywork.mapper.model.UserCompanyRecord;
import com.company.dailywork.mapper.model.UserRoleRecord;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.UserService;
import com.company.dailywork.web.dto.CreateUserRequest;
import com.company.dailywork.web.dto.RegisterRequest;
import com.company.dailywork.web.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserCompanyMapper userCompanyMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final CompanyCatalogMapper companyCatalogMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper,
                           UserCompanyMapper userCompanyMapper,
                           UserRoleMapper userRoleMapper,
                           RoleMapper roleMapper,
                           CompanyCatalogMapper companyCatalogMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userCompanyMapper = userCompanyMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.companyCatalogMapper = companyCatalogMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getByUsername(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("limit 1"));
        if (user != null) {
            hydrateRole(user);
            hydrateEmployment(user);
        }
        return user;
    }

    @Override
    public User register(RegisterRequest request) {
        if (getByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRole(UserRole.EMPLOYEE.getCode());
        user.setEmployedCompany(null);
        user.setStatus(1);
        userMapper.insert(user);
        bindSingleRole(user.getId(), user.getRole());
        bindEmployment(user, null);
        return user;
    }

    @Override
    public User createUser(CreateUserRequest request, SecurityUser operator) {
        if (getByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        UserRole targetRole = UserRole.fromCode(request.getRole());
        validateAssignable(operator, targetRole);

        User user = new User();
        String employedCompany = normalizeEmployedCompany(request.getEmployedCompany(), targetRole);
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRole(targetRole.getCode());
        user.setEmployedCompany(employedCompany);
        user.setStatus(1);
        userMapper.insert(user);
        bindSingleRole(user.getId(), user.getRole());
        bindEmployment(user, employedCompany);
        return user;
    }

    @Override
    public List<UserResponse> listUsers() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
            .orderByAsc(User::getId));
        hydrateRoles(users);
        hydrateEmployments(users);
        return users
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    public User updateRole(Long userId, String roleCode, SecurityUser operator) {
        User user = requireUser(userId);
        UserRole targetRole = UserRole.fromCode(roleCode);
        validateAssignable(operator, targetRole);
        validateManageTarget(operator, user);
        user.setRole(targetRole.getCode());
        String employedCompany = normalizeEmployedCompany(user.getEmployedCompany(), targetRole);
        user.setEmployedCompany(employedCompany);
        userMapper.updateById(user);
        bindSingleRole(user.getId(), user.getRole());
        bindEmployment(user, employedCompany);
        return user;
    }

    @Override
    public User updateStatus(Long userId, Integer status, SecurityUser operator) {
        User user = requireUser(userId);
        validateManageTarget(operator, user);
        user.setStatus(status);
        userMapper.updateById(user);
        return user;
    }

    @Override
    public User updateEmployedCompany(Long userId, String employedCompany, SecurityUser operator) {
        User user = requireUser(userId);
        validateManageTarget(operator, user);
        String normalizedCompany = normalizeEmployedCompany(employedCompany, UserRole.fromCode(user.getRole()));
        user.setEmployedCompany(normalizedCompany);
        userMapper.updateById(user);
        bindEmployment(user, normalizedCompany);
        return user;
    }

    @Override
    public User updateMyEmployedCompany(SecurityUser securityUser, String employedCompany) {
        User user = requireUser(securityUser.getUser().getId());
        UserRole role = UserRole.fromCode(user.getRole());
        if (role != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("Only employee can update own employed company");
        }
        String normalizedCompany = normalizeEmployedCompany(employedCompany, role);
        user.setEmployedCompany(normalizedCompany);
        userMapper.updateById(user);
        bindEmployment(user, normalizedCompany);
        return user;
    }

    @Override
    public void changeMyPassword(SecurityUser securityUser, String oldPassword, String newPassword) {
        User user = requireUser(securityUser.getUser().getId());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long userId, String newPassword, SecurityUser operator) {
        User user = requireUser(userId);
        validatePasswordResetTarget(operator, user);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        hydrateRole(user);
        hydrateEmployment(user);
        return user;
    }

    private void validateAssignable(SecurityUser operator, UserRole targetRole) {
        UserRole operatorRole = UserRole.fromCode(operator.getUser().getRole());
        if (!UserRole.assignableRolesBy(operatorRole).contains(targetRole)) {
            throw new IllegalArgumentException("Current role cannot assign target role");
        }
    }

    private void validateManageTarget(SecurityUser operator, User targetUser) {
        UserRole operatorRole = UserRole.fromCode(operator.getUser().getRole());
        UserRole targetRole = UserRole.fromCode(targetUser.getRole());

        if (operator.getUser().getId().equals(targetUser.getId()) && operatorRole != UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("You cannot modify your own admin account");
        }

        if (operatorRole == UserRole.ADMIN && targetRole == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Admin cannot manage super admin accounts");
        }
    }

    private void validatePasswordResetTarget(SecurityUser operator, User targetUser) {
        UserRole operatorRole = UserRole.fromCode(operator.getUser().getRole());
        UserRole targetRole = UserRole.fromCode(targetUser.getRole());

        if (operatorRole == UserRole.ADMIN && targetRole == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Admin cannot manage super admin accounts");
        }
    }

    private void bindSingleRole(Long userId, String roleCode) {
        Long roleId = userRoleMapper.findRoleIdByCode(roleCode);
        if (roleId == null) {
            Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, roleCode).last("limit 1"));
            if (role == null) {
                throw new IllegalArgumentException("Role not found: " + roleCode);
            }
            roleId = role.getId();
        }
        userRoleMapper.deleteByUserId(userId);
        userRoleMapper.insertUserRole(userId, roleId);
    }

    private void hydrateRole(User user) {
        List<String> codes = userRoleMapper.listRoleCodesByUserId(user.getId());
        String roleCode = resolvePrimaryRoleCode(codes, user.getRole());
        user.setRole(roleCode);
    }

    private void hydrateEmployment(User user) {
        String companyName = userCompanyMapper.findCompanyNameByUserId(user.getId());
        user.setEmployedCompany(normalizeNullableText(companyName));
    }

    private void hydrateRoles(List<User> users) {
        if (users.isEmpty()) {
            return;
        }
        List<Long> userIds = users.stream().map(User::getId).toList();
        List<UserRoleRecord> records = userRoleMapper.listRoleCodesByUserIds(userIds);
        Map<Long, List<String>> grouped = records.stream().collect(Collectors.groupingBy(
                UserRoleRecord::getUserId,
                Collectors.mapping(UserRoleRecord::getRoleCode, Collectors.toList())
        ));

        for (User user : users) {
            List<String> codes = grouped.getOrDefault(user.getId(), Collections.emptyList());
            user.setRole(resolvePrimaryRoleCode(codes, user.getRole()));
        }
    }

    private void hydrateEmployments(List<User> users) {
        if (users.isEmpty()) {
            return;
        }

        for (User user : users) {
            String companyName = userCompanyMapper.findCompanyNameByUserId(user.getId());
            user.setEmployedCompany(normalizeNullableText(companyName));
        }
    }

    private String resolvePrimaryRoleCode(List<String> roleCodes, String fallback) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return fallback == null ? UserRole.EMPLOYEE.getCode() : fallback;
        }

        Map<String, Integer> rank = Map.of(
                UserRole.SUPER_ADMIN.getCode(), 3,
                UserRole.ADMIN.getCode(), 2,
                UserRole.EMPLOYEE.getCode(), 1
        );

        return roleCodes.stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(code -> rank.getOrDefault(code, 0)))
                .orElse(fallback == null ? UserRole.EMPLOYEE.getCode() : fallback);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmployedCompany(),
                user.getRole(),
                user.getStatus(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    private void bindEmployment(User user, String employedCompany) {
        Long companyId = resolveCompanyId(employedCompany);
        userCompanyMapper.upsert(user.getId(), companyId);
    }

    private Long resolveCompanyId(String companyName) {
        String normalized = normalizeNullableText(companyName);
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

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeEmployedCompany(String employedCompany, UserRole targetRole) {
        if (targetRole != UserRole.EMPLOYEE) {
            return null;
        }
        return normalizeNullableText(employedCompany);
    }
}
