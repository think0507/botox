package com.botox.service;

import com.botox.domain.Room;
import com.botox.exception.NotFoundRoomException;
import com.botox.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

    public List<Room> getAllRoomByContent(String roomContent) {
        List<Room> rooms = roomRepository.findByRoomContent(roomContent);
        if (rooms.isEmpty()) {
            throw new NotFoundRoomException("해당 내용에 대한 방을 찾을 수 없습니다: " + roomContent);
        }
        return rooms;
    }
    public void saveRoom(Room room) {
        roomRepository.save(room);
    }
}