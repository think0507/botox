package com.botox.service;

import com.botox.domain.Chat;
import com.botox.domain.Room;
import com.botox.domain.User;
import com.botox.repository.ChatRepository;
import com.botox.repository.RoomRepository;
import com.botox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public Chat saveChatMessage(Long roomId, Long senderId, String message) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Chat chat = new Chat();
        chat.setRoom(room);
        chat.setSender(sender);
        chat.setContent(message);
        chat.setTimestamp(LocalDateTime.now());

        return chatRepository.save(chat);
    }

    public List<Chat> getChatMessagesByRoomId(Long roomId) {
        return chatRepository.findByRoomRoomNum(roomId);
    }
}