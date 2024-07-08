package com.botox.service;

import com.botox.constant.ProcessingStatus;
import com.botox.constant.ReportType;
import com.botox.domain.*;
import com.botox.repository.ReportRepository;
import com.botox.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // 신고하기 (게시글, 댓글, 사용자)
    public ReportResponse reportUser(ReportRequest reportRequest) {
        User reportingUser = userRepository.findById(reportRequest.getReportingUserId())
                .orElseThrow(() -> new RuntimeException("Reporting user not found"));
        User reportedUser = userRepository.findById(reportRequest.getReportedUserId())
                .orElseThrow(() -> new RuntimeException("Reported user not found"));

        Report report = new Report();
        report.setReportTime(LocalDateTime.now());
        report.setReportingUser(reportingUser);
        report.setReportedUser(reportedUser);
        report.setReasonForReport(reportRequest.getReasonForReport());
        report.setReportedPostId(reportRequest.getReportedPostId());
        report.setReportedCommentId(reportRequest.getReportedCommentId());
        report.setReportedChatId(reportRequest.getReportedChatId());
        report.setReportType(ReportType.WARNING);
        report.setProcessingStatus(ProcessingStatus.PENDING);

        Report savedReport = reportRepository.save(report);

        // Convert to ReportResponse
        ReportResponse response = new ReportResponse();
        response.setId(savedReport.getId());
        response.setReportTime(savedReport.getReportTime());
        response.setReasonForReport(savedReport.getReasonForReport());
        response.setReportedPostId(savedReport.getReportedPostId());
        response.setReportedCommentId(savedReport.getReportedCommentId());
        response.setReportedChatId(savedReport.getReportedChatId());
        response.setReportType(savedReport.getReportType());
        response.setProcessingStatus(savedReport.getProcessingStatus());

        ReportDTO reportingUserDTO = new ReportDTO();
        reportingUserDTO.setId(reportingUser.getId());
        response.setReportingUser(reportingUserDTO);

        ReportDTO reportedUserDTO = new ReportDTO();
        reportedUserDTO.setId(reportedUser.getId());
        response.setReportedUser(reportedUserDTO);

        return response;
    }

    //전체 신고 조회 (관리자용)
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    // 신고 조회 (관리자용)
    public Report getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }


}
