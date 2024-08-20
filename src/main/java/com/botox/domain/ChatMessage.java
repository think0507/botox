package com.botox.domain;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ChatMessage {

    private Long id;
    private Long chatRoomId;
    private String userId;
    private String name;
    private String message;
    private LocalDateTime timestamp;  // 날짜와 시간 추가

    public ChatMessage(Long id, Long chatRoomId, String name, String message, LocalDateTime timestamp, String userId) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.name = name;
        this.message = message;
        this.timestamp = timestamp;
    }
}