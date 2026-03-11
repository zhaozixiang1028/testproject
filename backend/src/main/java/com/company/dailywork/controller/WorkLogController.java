package com.company.dailywork.controller;

import com.company.dailywork.common.model.ApiResponse;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.WorkLogService;
import com.company.dailywork.web.dto.WorkLogResponse;
import com.company.dailywork.web.dto.WorkLogSaveRequest;
import com.company.dailywork.web.dto.WorkLogStatisticsResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class WorkLogController {

    private final WorkLogService workLogService;

    public WorkLogController(WorkLogService workLogService) {
        this.workLogService = workLogService;
    }

    @PostMapping("/daily")
    public ApiResponse<WorkLogResponse> saveDaily(@RequestBody @Valid WorkLogSaveRequest request,
                                                  @AuthenticationPrincipal SecurityUser user) {
        return ApiResponse.success("Saved", workLogService.saveOrUpdate(request, user));
    }

    @GetMapping
    public ApiResponse<List<WorkLogResponse>> list(@AuthenticationPrincipal SecurityUser user,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(workLogService.list(user, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkLogResponse> detail(@PathVariable Long id,
                                               @AuthenticationPrincipal SecurityUser user) {
        return ApiResponse.success(workLogService.getById(id, user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal SecurityUser user) {
        workLogService.delete(id, user);
        return ApiResponse.success("Deleted", null);
    }

    @GetMapping("/statistics")
    public ApiResponse<WorkLogStatisticsResponse> statistics(@AuthenticationPrincipal SecurityUser user,
                                                             @RequestParam(defaultValue = "week") String period) {
        return ApiResponse.success(workLogService.statistics(user, period));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal SecurityUser user,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String csv = workLogService.exportCsv(user, startDate, endDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=work-logs.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
