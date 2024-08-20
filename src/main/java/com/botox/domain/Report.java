package com.botox.domain;

import com.botox.constant.ProcessingStatus;
import com.botox.constant.ReportType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "report_time")
    private LocalDateTime reportTime;

    @ManyToOne
    @JoinColumn(name = "reporting_user_id", referencedColumnName = "id")
    private User reportingUser;

    @ManyToOne
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @Column(name = "feedback_result")
    private String feedbackResult;

    @Column(name = "reason_for_report", columnDefinition = "TEXT")
    private String reasonForReport;

    private Long reportedPostId;      // 신고받은 게시글 ID (nullable)
    private Long reportedCommentId;   // 신고받은 댓글 ID (nullable)
    private Long reportedChatId;      // 신고받은 채팅 ID (nullable)

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type")
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    private ProcessingStatus processingStatus;

    @Column(name = "reported_content_id")
    private Long reportedContentId;


}
