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
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus;

    @Column(name = "reported_content_id")
    private Long reportedContentId;
}