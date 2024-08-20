package com.botox.controller;

import com.botox.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations template;

    private final AtomicLong idGenerator = new AtomicLong(1);  // idGenerator 선언 및 초기화

    @MessageMapping("/message")
    public void sendMessage(ChatMessage message) {
        // message 객체에 방 번호(roomId)가 포함되어 있다고 가정합니다.
        // ID 설정
        message.setId(idGenerator.getAndIncrement());
        // 현재 시간을 타임스탬프로 설정
        message.setTimestamp(LocalDateTime.now());
        String destination = "/sub/chatroom/" + message.getChatRoomId();
        template.convertAndSend(destination, message);
    }
}