package com.botox.domain;

import com.botox.constant.ProcessingStatus;
import com.botox.constant.ReportType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    private LocalDateTime reportTime;

    @ManyToOne
    @JoinColumn(name = "reporting_user_id")
    private User reportingUser;

    @ManyToOne
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    private String feedbackResult;
    private String reasonForReport;

    @Enumerated(EnumType.STRING)
    private ReportType reportType; // enum: TEMPORARY_BAN, PERMANENT_BAN, WARNING

    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus; // enum: PENDING, RESOLVED, DISMISSED

    // Getters, setters, constructors
}
