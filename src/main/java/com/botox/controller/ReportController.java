package com.botox.controller;

import com.botox.domain.Report;
import com.botox.domain.ReportRequest;
import com.botox.domain.ReportResponse;
import com.botox.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    // 신고하기 (게시글, 댓글, 사용자)
    @PostMapping
    public ResponseForm<ReportResponse> reportUser(@RequestBody ReportRequest reportRequest) {
        ReportResponse createdReport = reportService.reportUser(reportRequest);
        return new ResponseForm<>(HttpStatus.CREATED, createdReport, "Report created successfully");
    }

    //전체 신고 조회 (관리자용)
    @GetMapping("/all")
    public ResponseForm<List<Report>> getAllReports() {
        List<Report> reports = reportService.getAllReports();
        return new ResponseForm<>(HttpStatus.OK, reports, "All reports retrieved successfully");
    }

    //신고조회 (관리자용)
    @GetMapping("/{reportId}")
    public ResponseForm<Report> getReport(@PathVariable Long reportId) {
        Report report = reportService.getReport(reportId);
        return new ResponseForm<>(HttpStatus.OK, report, "Report retrieved successfully");
    }


}
