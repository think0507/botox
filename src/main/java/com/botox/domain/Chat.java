package com.botox.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "inappropriate_report_id")
    private Report inappropriateReport;

    // Getters, setters, constructors
}

