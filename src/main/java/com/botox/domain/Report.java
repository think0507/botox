package com.botox.domain;

import com.botox.constant.ProcessingStatus;
import com.botox.constant.ReportType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter @Setter
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "report_time")
    private LocalDateTime reportTime;

    @ManyToOne
    @JoinColumn(name = "reporting_user_id", referencedColumnName = "user_id")
    private User reportingUser;

    @ManyToOne
    @JoinColumn(name = "reported_user_id", referencedColumnName = "user_id")
    private User reportedUser;

    @Column(name = "feedback_result")
    private String feedbackResult;

    @Column(name = "reason_for_report", columnDefinition = "TEXT")
    private String reasonForReport;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type")
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    private ProcessingStatus processingStatus;

    @Column(name = "reported_content_id")
    private Long reportedContentId;
}