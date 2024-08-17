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
    Map<String, Set<String>> rooms = new HashMap<>();

    // 방에 클라이언트 추가
    public void addClientToRoom(String roomNum, String userId) {
        // 방이 존재하지 않으면 생성
        rooms.put(roomNum, new HashSet<>());
        // 클라이언트를 방에 추가
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

            // 현재 클라이언트에게만 이벤트를 전송
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

            // roomNum에 있는 다른 클라이언트들에게 개별 매개변수로 이벤트 전달
            client.sendEvent("leave_room", new HashMap<String, String>() {{
                put("userId", userId);
                put("roomNum", roomNum);
            }});
        });

        // "offer" 이벤트 처리
        socketIOServer.addEventListener("offer", Map.class, (client, data, ackSender) -> {
            String roomNum = (String) data.get("roomNum");
            System.out.println("room " + roomNum + " send offer " + data);
            socketIOServer.getRoomOperations(roomNum).sendEvent("offer", data);
        });

        // "answer" 이벤트 처리
        socketIOServer.addEventListener("answer", Map.class, (client, data, ackSender) -> {
            String roomNum = (String) data.get("roomNum");
            System.out.println("room " + roomNum + " send answer " + data);
            socketIOServer.getRoomOperations(roomNum).sendEvent("answer", data);
        });

        // "ice_candidate" 이벤트 처리
        socketIOServer.addEventListener("ice_candidate", Map.class, (client, data, ackSender) -> {
            String roomNum = (String) data.get("roomNum");
            System.out.println("room " + roomNum + " send ice " + data);
            socketIOServer.getRoomOperations(roomNum).sendEvent("ice_candidate", data);
        });
    }
}
