//package com.botox.controller;
//
//import com.botox.domain.Chat;
//import com.botox.domain.ChatMessage;
//import com.botox.service.ChatService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.simp.SimpMessageSendingOperations;
//import org.springframework.stereotype.Controller;
//
//@Controller
//@RequiredArgsConstructor
//public class WebSocketChatController {
//    private final ChatService chatService;
//    private final SimpMessageSendingOperations messagingTemplate;
//
//    @MessageMapping("/chat.sendMessage")
//    public void sendMessage(ChatMessage chatMessage) {
//        Chat chat = chatService.saveChatMessage(chatMessage.getRoomId(), chatMessage.getSenderId(), chatMessage.getMessage());
//        messagingTemplate.convertAndSend("/sub/chat/room/" + chatMessage.getRoomId(), chat);
//    }
//}
