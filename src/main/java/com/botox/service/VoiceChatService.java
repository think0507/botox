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
    private final Map<String, Set<SocketIOClient>> rooms = new HashMap<>();
    // 클라이언트와 사용자 ID 매핑
    private final Map<SocketIOClient, String> clientToUserId = new HashMap<>();
    private final Map<String, SocketIOClient> userIdToClient = new HashMap<>();

    @Autowired
    public VoiceChatService(SocketIOServer socketIOServer) {

        // "enter_room" 이벤트 처리
        socketIOServer.addEventListener("enter_room", Map.class, (client, data, ackSender) -> {
            String userId = (String) data.get("userId");
            String roomNum = (String) data.get("roomNum");

            // 클라이언트와 사용자 ID 매핑 추가
            clientToUserId.put(client, userId);
            userIdToClient.put(userId, client);

            // 방에 클라이언트 추가
            addClientToRoom(roomNum, client);
            System.out.println("User " + userId + " entered room " + roomNum);

            // 방에 있는 모든 사용자에게 새로 입장한 사용자 정보 전송
//            Set<SocketIOClient> clientsInRoom = rooms.get(roomNum);
//            if (clientsInRoom != null) {
//                clientsInRoom.stream()
//                        .filter(c -> !c.equals(client)) // 자신 제외
//                        .forEach(c -> c.sendEvent("user_joined", new HashMap<String, String>() {{
//                            put("userId", userId);
//                            put("roomNum", roomNum);
//                        }}));
//            }

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

            // 방에서 클라이언트 제거
            removeClientFromRoom(roomNum, client);
            // 클라이언트와 사용자 ID 매핑 제거
            clientToUserId.remove(client);
            userIdToClient.remove(userId);

            System.out.println("User " + userId + " left room " + roomNum);

            // 나간 클라이언트에게만 "leave_room" 이벤트 전송
//            client.sendEvent("leave_room", new HashMap<String, String>() {{
//                put("userId", userId);
//                put("roomNum", roomNum);
//            }});

            // 방에 있는 모든 사용자에게 나간 사용자 정보 전송
            Set<SocketIOClient> clientsInRoom = rooms.get(roomNum);
            if (clientsInRoom != null) {
                clientsInRoom.stream()
                        .filter(c -> !c.equals(client)) // 자신 제외
                        .forEach(c -> c.sendEvent("user_left", new HashMap<String, String>() {{
                            put("userId", userId);
                            put("roomNum", roomNum);
                        }}));
            }
        });

        // "offer" 이벤트 처리
        socketIOServer.addEventListener("offer", Map.class, (client, data, ackSender) -> {
            String toUserId = (String) data.get("to");
            String fromUserId = (String) data.get("from");
            String roomNum = (String) data.get("roomNum");
            System.out.println("Sending offer from " + fromUserId + " to " + toUserId + " in room " + roomNum);

            // 방에 있는 특정 클라이언트에게만 offer 이벤트 전송
            SocketIOClient toClient = userIdToClient.get(toUserId);
            if (toClient != null) {
                toClient.sendEvent("offer", data);
            }
        });

        // "answer" 이벤트 처리
        socketIOServer.addEventListener("answer", Map.class, (client, data, ackSender) -> {
            String toUserId = (String) data.get("to");
            String fromUserId = (String) data.get("from");
            String roomNum = (String) data.get("roomNum");
            System.out.println("Sending answer from " + fromUserId + " to " + toUserId + " in room " + roomNum);

            // 방에 있는 특정 클라이언트에게만 answer 이벤트 전송
            SocketIOClient toClient = userIdToClient.get(toUserId);
            if (toClient != null) {
                toClient.sendEvent("answer", data);
            }
        });

        // "ice_candidate" 이벤트 처리
        socketIOServer.addEventListener("ice_candidate", Map.class, (client, data, ackSender) -> {
            String toUserId = (String) data.get("to");
            String fromUserId = (String) data.get("from");
            String roomNum = (String) data.get("roomNum");
            System.out.println("Sending ICE candidate from " + fromUserId + " to " + toUserId + " in room " + roomNum);

            // 방에 있는 특정 클라이언트에게만 ICE candidate 이벤트 전송
            SocketIOClient toClient = userIdToClient.get(toUserId);
            if (toClient != null) {
                toClient.sendEvent("ice_candidate", data);
            }
        });

    }

    private void addClientToRoom(String roomNum, SocketIOClient client) {
        rooms.putIfAbsent(roomNum, new HashSet<>());
        rooms.get(roomNum).add(client);
    }

    private void removeClientFromRoom(String roomNum, SocketIOClient client) {
        Set<SocketIOClient> clients = rooms.get(roomNum);
        if (clients != null) {
            clients.remove(client);
            if (clients.isEmpty()) {
                rooms.remove(roomNum);
            }
        }
    }
}
