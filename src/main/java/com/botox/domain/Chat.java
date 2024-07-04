package com.botox.domain;

import com.botox.domain.Room;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name = "chat")
@Getter
@Setter
@NoArgsConstructor
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
    private String content;  // Long에서 String으로 변경

    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "inappropriate_report_id")
    private Report inappropriateReport;
}

