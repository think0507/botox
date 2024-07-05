package com.botox.domain;

import com.botox.constant.ProcessingStatus;
import com.botox.constant.ReportType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime reportTime;

    @ManyToOne
    @JoinColumn(name = "reporting_user_id", nullable = false)
    private User reportingUser;

    @ManyToOne
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    private String feedbackResult;

    private String reasonForReport;

    private Long reportedPostId;      // 신고받은 게시글 ID (nullable)
    private Long reportedCommentId;   // 신고받은 댓글 ID (nullable)
    private Long reportedChatId;      // 신고받은 채팅 ID (nullable)

    @Enumerated(EnumType.STRING)
    private ReportType reportType = ReportType.WARNING; // 기본값 설정, enum: TEMPORARY_BAN, PERMANENT_BAN, WARNING

    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING; // 기본값 설정, enum: PENDING, RESOLVED, DISMISSED

    // Getters, setters, constructors
}
