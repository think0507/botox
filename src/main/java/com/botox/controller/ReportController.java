package com.botox.controller;

import com.botox.domain.Report;
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

    @PostMapping("/user")
    public ResponseForm<PostController.ReportResponse> reportUser(@RequestBody PostController.ReportRequest reportRequest) {
        PostController.ReportResponse createdReport = reportService.reportUser(reportRequest);
        return new ResponseForm<>(HttpStatus.CREATED, createdReport, "User report created successfully");
    }

    @PostMapping("/post")
    public ResponseForm<PostController.ReportResponse> reportPost(@RequestBody PostController.ReportRequest reportRequest) {
        PostController.ReportResponse createdReport = reportService.reportPost(reportRequest);
        return new ResponseForm<>(HttpStatus.CREATED, createdReport, "Post report created successfully");
    }

    @GetMapping("/all")
    public ResponseForm<List<Report>> getAllReports() {
        List<Report> reports = reportService.getAllReports();
        return new ResponseForm<>(HttpStatus.OK, reports, "All reports retrieved successfully");
    }

    @GetMapping("/{reportId}")
    public ResponseForm<Report> getReport(@PathVariable Long reportId) {
        Report report = reportService.getReport(reportId);
        return new ResponseForm<>(HttpStatus.OK, report, "Report retrieved successfully");
    }
}