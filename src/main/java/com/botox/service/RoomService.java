package com.botox.service;

import com.botox.controller.RoomApiController;
import com.botox.domain.Room;
import com.botox.domain.User;
import com.botox.exception.NotFoundRoomException;
import com.botox.repository.RoomRepository;
import com.botox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public List<Room> getAllRoomByContent(String roomContent) {
        List<Room> rooms = roomRepository.findByRoomContent(roomContent);
        if (rooms.isEmpty()) {
            throw new NotFoundRoomException("해당 내용에 대한 방을 찾을 수 없습니다: " + roomContent);
        }
        return rooms;
    }

    //방 생성 기능
    public Room saveRoom(RoomApiController.RoomForm roomForm) {
        Room room = new Room();
        room.setRoomTitle(roomForm.getRoomTitle());
        room.setRoomContent(roomForm.getRoomContent());
        room.setRoomType(roomForm.getRoomType());
        room.setGameName(roomForm.getGameName());

        // roomMasterId를 이용해 User 객체 설정(존재하는 사용자인지 인증)
        User roomMaster = userRepository.findById(roomForm.getRoomMasterId())
                .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + roomForm.getRoomMasterId()));

        room.setRoomMaster(roomMaster);
        room.setRoomStatus(roomForm.getRoomStatus());
        room.setRoomCapacityLimit(roomForm.getRoomCapacityLimit());
        room.setRoomUpdateTime(roomForm.getRoomUpdateTime().toLocalDateTime());
        room.setRoomCreateAt(LocalDateTime.now());

        return roomRepository.save(room);
    }

    //방 수정 기능
    public Room updateRoom(Long roomNum, RoomApiController.RoomForm roomForm) {
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        if (roomForm.getRoomTitle() != null) room.setRoomTitle(roomForm.getRoomTitle());
        if (roomForm.getRoomContent() != null) room.setRoomContent(roomForm.getRoomContent());
        if (roomForm.getRoomType() != null) room.setRoomType(roomForm.getRoomType());
        if (roomForm.getGameName() != null) room.setGameName(roomForm.getGameName());

        if (roomForm.getRoomMasterId() != null) {
            User roomMaster = userRepository.findById(roomForm.getRoomMasterId())
                    .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + roomForm.getRoomMasterId()));
            room.setRoomMaster(roomMaster);
        }

        if (roomForm.getRoomStatus() != null) room.setRoomStatus(roomForm.getRoomStatus());
        if (roomForm.getRoomCapacityLimit() != null) room.setRoomCapacityLimit(roomForm.getRoomCapacityLimit());

        room.setRoomUpdateTime(LocalDateTime.now());

        return roomRepository.save(room);
    }

    //방 삭제 기능
    public void deleteRoom(Long roomNum) {
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));
        roomRepository.delete(room);
    }

    // 방 나가기 기능
    public void leaveRoom(Long roomNum, Long userId) {
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));
        if (room.getRoomMaster().getUserId().equals(userId)) {
            roomRepository.delete(room);
        } else {
            int userCount = room.getRoomUserCount() != null ? room.getRoomUserCount() : 0;
            room.setRoomUserCount(userCount - 1);
            roomRepository.save(room);
        }
    }

    // 방 입장 기능
    public void joinRoom(Long roomNum) {
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));
        int userCount = room.getRoomUserCount() != null ? room.getRoomUserCount() : 0;
        room.setRoomUserCount(userCount + 1);
        roomRepository.save(room);
    }
}