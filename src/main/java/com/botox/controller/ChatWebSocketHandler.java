//package com.botox.controller;
//
//
//import com.botox.domain.User;
//import org.kurento.client.KurentoClient;
//import org.springframework.web.socket.*;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class ChatWebSocketHandler extends TextWebSocketHandler {  // 이름을 변경합니다.
//
//    private final KurentoClient kurentoClient;
//    private final Map<String, UserSession> userSessions = new ConcurrentHashMap<>();
//    User user = new User();
//
//
//    public ChatWebSocketHandler(KurentoClient kurentoClient) {
//        this.kurentoClient = kurentoClient;
//    }
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        UserSession userSession = new UserSession(session);
//        userSessions.put(session.getId(), userSession);
//        System.out.println("Chat Connection established with session ID: " + session.getId());
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//        System.out.println("Received message: " + payload);
//
//        Map<String, Object> msg = new ObjectMapper().readValue(payload, Map.class);
//        String id = (String) msg.get("id");
//
//        if ("message".equals(id)) {
//            handleMessage(session, msg);
//        }
//    }
//
//    private void handleMessage(WebSocketSession session, Map<String, Object> msg) {
//        String message = (String) msg.get("message");
//        userSessions.values().forEach(u -> {
//            try {
//                // 로그인시 닉네임 받아오게 해야함
//                String randomName = "User" + (int) (Math.random() * 1000);
//                if (!u.getSession().getId().equals(session.getId()) && u.getSession().isOpen()) {
//                    ObjectNode response = new ObjectMapper().createObjectNode();
//                    response.put("id", "message");
//                    response.put("message", message);
//                    response.put("sessionId", session.getId());
//                    response.put("nickName", randomName); // 로그인시 닉네임 받아오게 해야함
//                    u.getSession().sendMessage(new TextMessage(response.toString()));
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        userSessions.remove(session.getId());
//        System.out.println("Chat Connection closed with session ID: " + session.getId());
//    }
//}