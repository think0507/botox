package com.botox.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class VoiceChatService {

    // 방과 클라이언트 목록을 저장하는 Map
    // SocketIOClient 대신 userId를 저장하도록 수정
    Map<String, Set<String>> rooms = new HashMap<>();

    // 방에 클라이언트 추가
    public void addClientToRoom(String roomNum, String userId) {
        rooms.putIfAbsent(roomNum, new HashSet<>());
        rooms.get(roomNum).add(userId);
    }

    // 방에서 클라이언트 제거
    public void removeClientFromRoom(String roomNum, String userId) {
        Set<String> clients = rooms.get(roomNum);
        if (clients != null) {
            clients.remove(userId);
            if (clients.isEmpty()) {
                rooms.remove(roomNum);
            }
        }
    }

    @Autowired
    public VoiceChatService(SocketIOServer socketIOServer) {

        // "enter_room" 이벤트 처리
        socketIOServer.addEventListener("enter_room", Map.class, (client, data, ackSender) -> {
            String userId = (String) data.get("userId");
            String roomNum = (String) data.get("roomNum");
            addClientToRoom(roomNum, userId);
            System.out.println("User " + userId + " entered room " + roomNum);

            // 방에 있는 모든 사용자 목록을 로그로 출력
            Set<String> clientsInRoom = rooms.get(roomNum);
            if (clientsInRoom != null) {
                System.out.println("Current users in room " + roomNum + ":");
                clientsInRoom.forEach(System.out::println);
            } else {
                System.out.println("Room " + roomNum + " is empty.");
            }

            // 입장한 클라이언트에게만 "enter_room" 이벤트 전송
            client.sendEvent("enter_room", new HashMap<String, String>() {{
                put("userId", userId);
                put("roomNum", roomNum);
            }});
        });

        // "leave_room" 이벤트 처리
        socketIOServer.addEventListener("leave_room", Map.class, (client, data, ackSender) -> {
            String userId = (String) data.get("userId");
            String roomNum = (String) data.get("roomNum");
            removeClientFromRoom(roomNum, userId);
            System.out.println("User " + userId + " left room " + roomNum);

            // 방을 떠난 클라이언트에게만 이벤트 전송
            client.sendEvent("leave_room", new HashMap<String, String>() {{
                put("userId", userId);
                put("roomNum", roomNum);
            }});
        });

        // "offer" 이벤트 처리
        socketIOServer.addEventListener("offer", Map.class, (client, data, ackSender) -> {
            String toUserId = (String) data.get("to");
            String fromUserId = (String) data.get("from");
            String roomNum = (String) data.get("roomNum");
            System.out.println("Sending offer from " + fromUserId + " to " + toUserId + " in room " + roomNum);

            // 특정 클라이언트에게만 offer 이벤트 전송
            rooms.get(roomNum).stream()
                    .filter(userId -> userId.equals(toUserId))
                    .forEach(userId -> client.sendEvent("offer", data));
        });

        // "answer" 이벤트 처리
        socketIOServer.addEventListener("answer", Map.class, (client, data, ackSender) -> {
            String toUserId = (String) data.get("to");
            String fromUserId = (String) data.get("from");
            String roomNum = (String) data.get("roomNum");
            System.out.println("Sending answer from " + fromUserId + " to " + toUserId + " in room " + roomNum);

            // 특정 클라이언트에게만 answer 이벤트 전송
            rooms.get(roomNum).stream()
                    .filter(userId -> userId.equals(toUserId))
                    .forEach(userId -> client.sendEvent("answer", data));
        });

        // "ice_candidate" 이벤트 처리
        socketIOServer.addEventListener("ice_candidate", Map.class, (client, data, ackSender) -> {
            String toUserId = (String) data.get("to");
            String fromUserId = (String) data.get("from");
            String roomNum = (String) data.get("roomNum");
            System.out.println("Sending ICE candidate from " + fromUserId + " to " + toUserId + " in room " + roomNum);

            // 특정 클라이언트에게만 ICE candidate 이벤트 전송
            rooms.get(roomNum).stream()
                    .filter(userId -> userId.equals(toUserId))
                    .forEach(userId -> client.sendEvent("ice_candidate", data));
        });

    }
}
