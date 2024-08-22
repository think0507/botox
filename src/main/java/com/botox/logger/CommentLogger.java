package com.botox.logger;

import com.botox.controller.CommentController;
import com.botox.util.CustomIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommentLogger {
    private static final Logger logger = LoggerFactory.getLogger(CommentLogger.class.getName());

    public static void logCommentReport(
            String logType,
            String action,
            String commentId,
            String postId,
            String reportingUserId,
            String reportingUserNickname,
            String reportedUserId,
            String reportedUserNickname,
            String reasonForReport,
            HttpServletRequest request
    ) {
        String message = String.format("사용자 %s님이 사용자 %s님의 게시글 %s에 있는 댓글 %s을(를) 신고했습니다.", reportingUserId, commentId, postId, reportedUserId);
        String reason = String.format("신고 내역: %s", reasonForReport);
        String logMessage = String.format("| %-5s | %-20s | %-20s | %s | %s",
                action,
                message,
                reason,
                CustomIpUtil.getClientIp(request),
                request.getHeader("User-Agent")
        );
        logger.info(logMessage);
    }
}