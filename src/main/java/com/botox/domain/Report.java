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
    private Long reportId;

    private LocalDateTime reportTime;

    @ManyToOne
    @JoinColumn(name = "reporting_user_id")
    private User reportingUser;

    // reported_user_id를 FK가 아닌 일반 컬럼으로 관리
    @Column(name = "reported_user_id")
    private Long reportedUserId;

    private String feedbackResult;
    private String reasonForReport;

    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus;

    // 신고된 콘텐츠에 대한 참조
    @Column(name = "reported_content_id")
    private Long reportedContentId;

    // Getters, setters, constructors


}