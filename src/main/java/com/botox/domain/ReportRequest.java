package com.botox.domain;

import com.botox.constant.ReportType;
import lombok.Data;

@Data
public class ReportRequest {
    private Long reportingUserId;     // 신고하는 사용자 ID
    private Long reportedUserId;      // 신고받는 사용자 ID
    private String reasonForReport;   // 신고 이유
    private Long reportedPostId;      // 신고받는 게시글 ID (nullable)
    private Long reportedCommentId;   // 신고받는 댓글 ID (nullable)
    private Long reportedChatId;      // 신고받는 채팅 ID (nullable)
}
