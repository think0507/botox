package com.botox.controller;

import com.botox.domain.Chat;
import com.botox.domain.Room;
import com.botox.domain.User;
import com.botox.repository.ChatRepository;
import com.botox.repository.RoomRepository;
import com.botox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class TextChatController extends TextWebSocketHandler {

    // 방별로 WebSocket 세션을 관리하기 위한 맵
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    // 세션과 유저 매핑을 관리하기 위한 맵
    private final Map<WebSocketSession, User> sessionUserMap = new ConcurrentHashMap<>();

    // JSON 파싱을 위한 ObjectMapper
    private final ObjectMapper mapper = new ObjectMapper();

    // 필요한 레포지토리들을 주입받음
    private final ChatRepository chatRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 클라이언트가 WebSocket에 연결되었을 때 호출
        System.out.println("Text WebSocket Client Connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트로부터 메시지를 수신했을 때 호출
        Map<String, Object> messageMap = mapper.readValue(message.getPayload(), Map.class);
        String id = (String) messageMap.get("id");

        // 메시지 타입에 따라 처리 분기
        switch (id) {
            case "joinRoom":
                joinRoom(session, messageMap);
                break;
            case "message":
                broadcastMessage(session, messageMap);
                break;
            default:
                break;
        }
    }

    private void joinRoom(WebSocketSession session, Map<String, Object> messageMap) {
        // 사용자가 특정 방에 참여할 때 호출
        String roomId = (String) messageMap.get("roomId");
        String userId = (String) messageMap.get("userId");

        // 사용자 정보를 가져와서 세션과 매핑
        User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
        if (user != null) {
            sessionUserMap.put(session, user);
            rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
            System.out.println("Client joined room: " + roomId);
        }
    }

    private void broadcastMessage(WebSocketSession session, Map<String, Object> messageMap) throws IOException {
        // 사용자가 메시지를 보낼 때 호출
        String roomId = (String) messageMap.get("roomId");
        String messageContent = (String) messageMap.get("message");

        // 메시지를 데이터베이스에 저장
        Room room = roomRepository.findById(Long.valueOf(roomId)).orElse(null);
        User sender = sessionUserMap.get(session);

        if (room != null && sender != null) {
            Chat chatMessage = new Chat();
            chatMessage.setRoom(room);
            chatMessage.setSender(sender);
            chatMessage.setContent(messageContent);
            chatMessage.setTimestamp(LocalDateTime.now());
            chatRepository.save(chatMessage);

            // 메시지를 해당 방의 모든 클라이언트에게 브로드캐스트
            Set<WebSocketSession> roomSessions = rooms.get(roomId);
            if (roomSessions != null) {
                for (WebSocketSession roomSession : roomSessions) {
                    if (roomSession.isOpen()) {
                        roomSession.sendMessage(new TextMessage(mapper.writeValueAsString(Map.of(
                                "id", "message",
                                "roomId", roomId,
                                "sessionId", session.getId(),
                                "senderId", sender.getId(),
                                "senderNickname", sender.getUserNickname(),
                                "message", messageContent
                        ))));
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 클라이언트가 WebSocket 연결을 종료했을 때 호출
        rooms.values().forEach(sessions -> sessions.remove(session));
        sessionUserMap.remove(session);
        System.out.println("Text WebSocket Client Disconnected: " + session.getId());
    }

}