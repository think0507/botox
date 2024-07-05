package com.botox.domain;

import com.botox.constant.ProcessingStatus;
import com.botox.constant.ReportType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportResponse {
    private Long id;
    private LocalDateTime reportTime;
    private UserIdOnlyDTO reportingUser;
    private UserIdOnlyDTO reportedUser;
    private String feedbackResult;
    private String reasonForReport;
    private Long reportedPostId;
    private Long reportedCommentId;
    private Long reportedChatId;
    private ReportType reportType;
    private ProcessingStatus processingStatus;
}