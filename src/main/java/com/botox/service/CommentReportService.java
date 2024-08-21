package com.botox.service;

import com.botox.constant.ProcessingStatus;
import com.botox.constant.ReportType;
import com.botox.controller.CommentController;
import com.botox.domain.Comment;
import com.botox.domain.Report;
import com.botox.domain.User;
import com.botox.exception.NotFoundCommentException;
import com.botox.repository.CommentRepository;
import com.botox.repository.ReportRepository;
import com.botox.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentReportService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public CommentController.ReportForm reportComment(CommentController.ReportForm reportForm) {
        try {
            User reportingUser = userRepository.findById(reportForm.getReportingUserId())
                    .orElseThrow(() -> new RuntimeException("Reporting user not found"));
            User reportedUser = userRepository.findById(reportForm.getReportedUserId())
                    .orElseThrow(() -> new RuntimeException("Reported user not found"));
            Comment reportedComment = commentRepository.findById(reportForm.getReportedContentId())
                    .orElseThrow(() -> new NotFoundCommentException("Reported comment not found"));


            // 예를 들어, 로그를 남길 수 있습니다.
            log.info("Comment with ID {} reported by user {} against user {}",
                    reportedComment.getCommentId(), reportingUser.getId(), reportedUser.getId());



            Report report = Report.builder()
                    .reportTime(LocalDateTime.now())
                    .reportingUser(reportingUser)
                    .reportedUser(reportedUser)
                    .feedbackResult(reportForm.getFeedbackResult())
                    .reasonForReport(reportForm.getReasonForReport())
                    .reportType(reportForm.getReportType())
                    .processingStatus(ProcessingStatus.PENDING)
                    .reportedContentId(reportForm.getReportedContentId())
                    .build();

            reportRepository.save(report);

            return CommentController.ReportForm.builder()
                    .reportingUserId(reportingUser.getId())
                    .reportingUserNickname(reportingUser.getUserNickname())
                    .reportedUserId(reportedUser.getId())
                    .reportedUserNickname(reportedUser.getUserNickname())
                    .reportedContentId(reportForm.getReportedContentId())
                    .feedbackResult(reportForm.getFeedbackResult())
                    .reasonForReport(reportForm.getReasonForReport())
                    .reportType(reportForm.getReportType())
                    .build();

        } catch (Exception e) {
            log.error("Error in reportComment", e);
            throw e;
        }
    }
}