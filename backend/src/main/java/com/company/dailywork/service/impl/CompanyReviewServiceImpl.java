package com.company.dailywork.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.dailywork.common.model.UserRole;
import com.company.dailywork.entity.CompanyReview;
import com.company.dailywork.entity.ReviewAuditLog;
import com.company.dailywork.entity.User;
import com.company.dailywork.mapper.CompanyReviewMapper;
import com.company.dailywork.mapper.ReviewAuditLogMapper;
import com.company.dailywork.mapper.UserMapper;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.CompanyReviewService;
import com.company.dailywork.web.dto.CompanyReviewResponse;
import com.company.dailywork.web.dto.CompanyReviewSubmitRequest;
import com.company.dailywork.web.dto.ReviewAuditLogResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CompanyReviewServiceImpl implements CompanyReviewService {

    private final CompanyReviewMapper companyReviewMapper;
    private final ReviewAuditLogMapper reviewAuditLogMapper;
    private final UserMapper userMapper;

    public CompanyReviewServiceImpl(CompanyReviewMapper companyReviewMapper,
                                    ReviewAuditLogMapper reviewAuditLogMapper,
                                    UserMapper userMapper) {
        this.companyReviewMapper = companyReviewMapper;
        this.reviewAuditLogMapper = reviewAuditLogMapper;
        this.userMapper = userMapper;
    }

    @Override
    public CompanyReviewResponse submit(CompanyReviewSubmitRequest request, SecurityUser user) {
        UserRole role = UserRole.fromCode(user.getUser().getRole());
        CompanyReview entity = new CompanyReview();
        entity.setUserId(user.getUser().getId());
        if (role == UserRole.EMPLOYEE) {
            String employedCompany = normalizeCompany(user.getUser().getEmployedCompany());
            if (employedCompany == null) {
                throw new IllegalArgumentException("No employed company is set for current employee");
            }
            entity.setCompanyName(employedCompany);
            entity.setReviewStatus("APPROVED");
        } else {
            entity.setCompanyName(requireCompanyName(request.getCompanyName()));
            entity.setReviewStatus("PENDING");
        }
        entity.setCultureScore(request.getCultureScore());
        entity.setTeamScore(request.getTeamScore());
        entity.setGrowthScore(request.getGrowthScore());
        entity.setSalaryScore(request.getSalaryScore());
        entity.setBalanceScore(request.getBalanceScore());
        entity.setAnonymousMode(Boolean.TRUE.equals(request.getAnonymousMode()) ? 1 : 0);
        entity.setPublicVisible(1);
        entity.setReviewContent(request.getReviewContent());
        companyReviewMapper.insert(entity);
        User reviewUser = userMapper.selectById(entity.getUserId());
        String username = reviewUser == null ? "unknown" : reviewUser.getUsername();
        return toResponse(entity, username, user.getUser().getId());
    }

    @Override
    public CompanyReviewResponse update(Long reviewId, CompanyReviewSubmitRequest request, SecurityUser user) {
        CompanyReview entity = companyReviewMapper.selectById(reviewId);
        if (entity == null) {
            throw new IllegalArgumentException("Review not found");
        }

        UserRole role = UserRole.fromCode(user.getUser().getRole());
        boolean isOwner = user.getUser().getId().equals(entity.getUserId());
        if (!isOwner && role == UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("No permission to edit this review");
        }

        if (role == UserRole.EMPLOYEE) {
            String employedCompany = normalizeCompany(user.getUser().getEmployedCompany());
            if (employedCompany == null) {
                throw new IllegalArgumentException("No employed company is set for current employee");
            }
            entity.setCompanyName(employedCompany);
            entity.setReviewStatus("APPROVED");
        } else {
            entity.setCompanyName(requireCompanyName(request.getCompanyName()));
            if (!"PENDING".equals(entity.getReviewStatus())) {
                entity.setReviewStatus("PENDING");
            }
        }

        entity.setCultureScore(request.getCultureScore());
        entity.setTeamScore(request.getTeamScore());
        entity.setGrowthScore(request.getGrowthScore());
        entity.setSalaryScore(request.getSalaryScore());
        entity.setBalanceScore(request.getBalanceScore());
        entity.setAnonymousMode(Boolean.TRUE.equals(request.getAnonymousMode()) ? 1 : 0);
        entity.setPublicVisible(1);
        entity.setReviewContent(request.getReviewContent());
        companyReviewMapper.updateById(entity);

        return toResponse(entity, user.getUser().getUsername(), user.getUser().getId());
    }

    @Override
    public List<CompanyReviewResponse> list(String companyName, SecurityUser user) {
        LambdaQueryWrapper<CompanyReview> query = new LambdaQueryWrapper<CompanyReview>()
                .orderByDesc(CompanyReview::getId);

        String normalizedCompany = normalizeCompany(companyName);
        if (normalizedCompany != null) {
            query.eq(CompanyReview::getCompanyName, normalizedCompany);
        }

        List<CompanyReview> list = companyReviewMapper.selectList(query);
        Map<Long, String> usernames = loadUsernames(list);

        return list.stream()
            .map(item -> toResponse(item, usernames.getOrDefault(item.getUserId(), "unknown"), user.getUser().getId()))
                .toList();
    }

    @Override
    public CompanyReviewResponse verify(Long reviewId, String reviewStatus, String remark, SecurityUser operator) {
        UserRole role = UserRole.fromCode(operator.getUser().getRole());
        if (role == UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("Only admin can verify reviews");
        }

        String normalized = reviewStatus == null ? "" : reviewStatus.trim().toUpperCase();
        if (!"APPROVED".equals(normalized) && !"REJECTED".equals(normalized)) {
            throw new IllegalArgumentException("reviewStatus must be APPROVED or REJECTED");
        }

        CompanyReview entity = companyReviewMapper.selectById(reviewId);
        if (entity == null) {
            throw new IllegalArgumentException("Review not found");
        }

        String oldStatus = entity.getReviewStatus();

        entity.setReviewStatus(normalized);
        companyReviewMapper.updateById(entity);

        ReviewAuditLog log = new ReviewAuditLog();
        log.setReviewId(entity.getId());
        log.setOperatorUserId(operator.getUser().getId());
        log.setOperatorUsername(operator.getUser().getUsername());
        log.setOldStatus(oldStatus == null ? "PENDING" : oldStatus);
        log.setNewStatus(normalized);
        log.setRemark(remark);
        reviewAuditLogMapper.insert(log);

        User reviewUser = userMapper.selectById(entity.getUserId());
        String username = reviewUser == null ? "unknown" : reviewUser.getUsername();
        return toResponse(entity, username, operator.getUser().getId());
    }

    @Override
    public void delete(Long reviewId, SecurityUser operator) {
        UserRole role = UserRole.fromCode(operator.getUser().getRole());

        CompanyReview entity = companyReviewMapper.selectById(reviewId);
        if (entity == null) {
            throw new IllegalArgumentException("Review not found");
        }

        boolean isOwner = operator.getUser().getId().equals(entity.getUserId());
        if (role == UserRole.EMPLOYEE && !isOwner) {
            throw new IllegalArgumentException("No permission to delete this review");
        }

        User reviewUser = userMapper.selectById(entity.getUserId());
        if (reviewUser == null) {
            throw new IllegalArgumentException("Review owner not found");
        }

        if (role != UserRole.EMPLOYEE) {
            UserRole reviewUserRole = UserRole.fromCode(reviewUser.getRole());
            if (reviewUserRole != UserRole.EMPLOYEE) {
                throw new IllegalArgumentException("Only employee reviews can be deleted by admin");
            }
        }

        reviewAuditLogMapper.delete(new LambdaQueryWrapper<ReviewAuditLog>()
                .eq(ReviewAuditLog::getReviewId, reviewId));
        companyReviewMapper.deleteById(reviewId);
    }

    @Override
    public List<ReviewAuditLogResponse> listAuditLogs(Long reviewId) {
        return reviewAuditLogMapper.selectList(new LambdaQueryWrapper<ReviewAuditLog>()
                        .eq(ReviewAuditLog::getReviewId, reviewId)
                        .orderByDesc(ReviewAuditLog::getId))
                .stream()
                .map(log -> new ReviewAuditLogResponse(
                        log.getId(),
                        log.getReviewId(),
                        log.getOperatorUserId(),
                        log.getOperatorUsername(),
                        log.getOldStatus(),
                        log.getNewStatus(),
                        log.getRemark(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    private CompanyReviewResponse toResponse(CompanyReview item, String username, Long viewerUserId) {
        String displayUsername = username;
        if (item.getAnonymousMode() != null
                && item.getAnonymousMode() == 1
                && viewerUserId != null
                && !viewerUserId.equals(item.getUserId())) {
            displayUsername = "匿名用户";
        }

        return new CompanyReviewResponse(
                item.getId(),
            item.getUserId(),
            displayUsername,
                item.getCompanyName(),
                item.getCultureScore(),
                item.getTeamScore(),
                item.getGrowthScore(),
                item.getSalaryScore(),
                item.getBalanceScore(),
                item.getAnonymousMode(),
                item.getPublicVisible(),
                item.getReviewContent(),
                item.getReviewStatus(),
                item.getCreatedAt()
        );
    }

    private Map<Long, String> loadUsernames(List<CompanyReview> reviews) {
        Set<Long> userIds = reviews.stream()
                .map(CompanyReview::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    private String normalizeCompany(String companyName) {
        if (companyName == null) {
            return null;
        }
        String normalized = companyName.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String requireCompanyName(String companyName) {
        String normalized = normalizeCompany(companyName);
        if (normalized == null) {
            throw new IllegalArgumentException("Company name is required");
        }
        return normalized;
    }
}
