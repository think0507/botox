package com.botox.service;

import com.botox.constant.ProcessingStatus;
import com.botox.constant.ReportType;
import com.botox.controller.PostController;
import com.botox.domain.*;
import com.botox.repository.ReportRepository;
import com.botox.repository.UserRepository;
import com.botox.repository.PostRepository;
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
    private final PostRepository postRepository;

    public PostController.ReportResponse reportUser(PostController.ReportRequest reportRequest) {
        User reportingUser = userRepository.findById(reportRequest.getReportingUserId())
                .orElseThrow(() -> new RuntimeException("Reporting user not found"));
        User reportedUser = userRepository.findById(reportRequest.getReportedUserId())
                .orElseThrow(() -> new RuntimeException("Reported user not found"));

        Report report = new Report();
        report.setReportTime(LocalDateTime.now());
        report.setReportingUser(reportingUser);
        report.setReportedUser(reportedUser);
        report.setReasonForReport(reportRequest.getReasonForReport());
        report.setReportType(ReportType.valueOf(reportRequest.getReportType()));
        report.setProcessingStatus(ProcessingStatus.PENDING);
        report.setFeedbackResult(reportRequest.getFeedbackResult());

        Report savedReport = reportRepository.save(report);

        return createReportResponse(savedReport);
    }

    public PostController.ReportResponse reportPost(PostController.ReportRequest reportRequest) {
        User reportingUser = userRepository.findById(reportRequest.getReportingUserId())
                .orElseThrow(() -> new RuntimeException("Reporting user not found"));
        Post reportedPost = postRepository.findById(reportRequest.getReportedPostId())
                .orElseThrow(() -> new RuntimeException("Reported post not found"));
        User reportedUser = reportedPost.getUser();

        Report report = new Report();
        report.setReportTime(LocalDateTime.now());
        report.setReportingUser(reportingUser);
        report.setReportedUser(reportedUser);
        report.setReportedPostId(reportRequest.getReportedPostId());
        report.setReasonForReport(reportRequest.getReasonForReport());
        report.setReportType(ReportType.valueOf(reportRequest.getReportType()));
        report.setProcessingStatus(ProcessingStatus.PENDING);
        report.setFeedbackResult(reportRequest.getFeedbackResult());

        Report savedReport = reportRepository.save(report);

        return createReportResponse(savedReport);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Report getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }

    private PostController.ReportResponse createReportResponse(Report report) {
        PostController.ReportResponse response = new PostController.ReportResponse();
        response.setStatus("OK");

        PostController.ReportResponse.ReportData data = new PostController.ReportResponse.ReportData();
        data.setReportingUserId(report.getReportingUser().getId());
        data.setReportingUserNickname(report.getReportingUser().getUserNickname());
        data.setReportedUserId(report.getReportedUser().getId());
        data.setReportedUserNickname(report.getReportedUser().getUserNickname());
        data.setReportedPostId(report.getReportedPostId());
        data.setFeedbackResult(report.getFeedbackResult());
        data.setReasonForReport(report.getReasonForReport());
        data.setReportType(report.getReportType().name());

        response.setData(data);
        response.setMessage("게시글 신고가 성공적으로 접수되었습니다.");

        return response;
    }

}