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
    Map<String, Set<SocketIOClient>> rooms = new HashMap<>();

    // 방에 클라이언트 추가
    public void addClientToRoom(String roomNum, SocketIOClient client) {
        rooms.putIfAbsent(roomNum, new HashSet<>());
        rooms.get(roomNum).add(client);
    }

    // 방에서 클라이언트 제거
    public void removeClientFromRoom(String roomNum, SocketIOClient client) {
        Set<SocketIOClient> clients = rooms.get(roomNum);
        if (clients != null) {
            clients.remove(client);
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
            addClientToRoom(roomNum, client);
            System.out.println("User " + userId + " entered room " + roomNum);

            // 입장한 클라이언트에게만 이벤트 전송
            client.sendEvent("enter_room", new HashMap<String, String>() {{
                put("userId", userId);
                put("roomNum", roomNum);
            }});
        });

        // "leave_room" 이벤트 처리
        socketIOServer.addEventListener("leave_room", Map.class, (client, data, ackSender) -> {
            String userId = (String) data.get("userId");
            String roomNum = (String) data.get("roomNum");
            removeClientFromRoom(roomNum, client);
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
            String roomNum = (String) data.get("roomNum");
            System.out.println("Sending offer to " + toUserId + " in room " + roomNum);

            // 특정 클라이언트에게만 offer 이벤트 전송
            rooms.get(roomNum).stream()
                    .filter(c -> {
                        String userId = c.getHandshakeData().getSingleUrlParam("userId");
                        if (userId == null) {
                            System.out.println("UserId is null for client: " + c);
                        }
                        return userId != null && userId.equals(toUserId);
                    })
                    .forEach(c -> c.sendEvent("offer", data));
        });


        // "answer" 이벤트 처리
        socketIOServer.addEventListener("answer", Map.class, (client, data, ackSender) -> {
            String toUserId = (String) data.get("to");
            String roomNum = (String) data.get("roomNum");
            System.out.println("Sending answer to " + toUserId + " in room " + roomNum);

            // 특정 클라이언트에게만 answer 이벤트 전송
            rooms.get(roomNum).stream()
                    .filter(c -> c.getHandshakeData().getSingleUrlParam("userId").equals(toUserId))
                    .forEach(c -> c.sendEvent("answer", data));
        });

        // "ice_candidate" 이벤트 처리
        socketIOServer.addEventListener("ice_candidate", Map.class, (client, data, ackSender) -> {
            String toUserId = (String) data.get("to");
            String roomNum = (String) data.get("roomNum");
            System.out.println("Sending ICE candidate to " + toUserId + " in room " + roomNum);

            // 특정 클라이언트에게만 ICE candidate 이벤트 전송
            rooms.get(roomNum).stream()
                    .filter(c -> {
                        String userId = c.getHandshakeData().getSingleUrlParam("userId");
                        return toUserId.equals(userId);
                    })
                    .forEach(c -> c.sendEvent("ice_candidate", data));
        });

    }
}
