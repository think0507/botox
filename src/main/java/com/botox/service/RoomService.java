package com.botox.service;

import com.botox.controller.RoomApiController;
import com.botox.domain.Room;
import com.botox.domain.User;
import com.botox.exception.NotFoundRoomException;
import com.botox.repository.RoomRepository;
import com.botox.repository.UserRepository;
import com.botox.repository.query.RoomRepositoryQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    //방 조회 기능
    public List<Room> getAllRoomByContent(String roomContent) {
        //roomRepository에서 입력받은 roomContent와 일치하는 데이터들을 List<Room> 타입의 변수 rooms에 저장.
        List<Room> rooms = roomRepository.findByRoomContent(roomContent);
        //rooms가 비었으면 에러 반환
        if (rooms.isEmpty()) {
            throw new NotFoundRoomException("해당 내용에 대한 방을 찾을 수 없습니다: " + roomContent);
        }
        return rooms;
    }

    //방 생성 기능
    public Room saveRoom(RoomApiController.RoomForm roomForm) {
        //Room 객체 room 생성
        Room room = new Room();
        //생성한 room 객체에 roomForm 안에 있던 RoomTitle을 가져와서 저장
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

        //room 객체 안에 모두 저장되있으면 반환 room 으로
        return roomRepository.save(room);
    }

    // 방 수정 기능
    public Room updateRoom(Long roomNum, RoomApiController.RoomForm roomForm) {
        // roomNum을 이용해 Room 객체를 찾습니다. 해당 Room이 없으면 NotFoundRoomException 예외를 발생시킵니다.
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        // roomForm에 새로운 제목이 있으면 room 객체에 설정합니다.
        if (roomForm.getRoomTitle() != null) room.setRoomTitle(roomForm.getRoomTitle());
        // roomForm에 새로운 내용이 있으면 room 객체에 설정합니다.
        if (roomForm.getRoomContent() != null) room.setRoomContent(roomForm.getRoomContent());
        // roomForm에 새로운 방 타입이 있으면 room 객체에 설정합니다.
        if (roomForm.getRoomType() != null) room.setRoomType(roomForm.getRoomType());
        // roomForm에 새로운 게임 이름이 있으면 room 객체에 설정합니다.
        if (roomForm.getGameName() != null) room.setGameName(roomForm.getGameName());

        // roomForm에 새로운 roomMasterId가 있으면 해당 User 객체를 찾아 room 객체에 설정합니다. 없으면 NotFoundRoomException 예외를 발생시킵니다.
        if (roomForm.getRoomMasterId() != null) {
            User roomMaster = userRepository.findById(roomForm.getRoomMasterId())
                    .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + roomForm.getRoomMasterId()));
            room.setRoomMaster(roomMaster);
        }

        // roomForm에 새로운 방 상태가 있으면 room 객체에 설정합니다.
        if (roomForm.getRoomStatus() != null) room.setRoomStatus(roomForm.getRoomStatus());
        // roomForm에 새로운 방 인원 제한이 있으면 room 객체에 설정합니다.
        if (roomForm.getRoomCapacityLimit() != null) room.setRoomCapacityLimit(roomForm.getRoomCapacityLimit());

        // 현재 시간을 roomUpdateTime으로 설정합니다.
        room.setRoomUpdateTime(LocalDateTime.now());

        // room 객체를 저장하고 반환합니다.
        return roomRepository.save(room);
    }

    // 방 삭제 기능
    public void deleteRoom(Long roomNum) {
        // roomNum을 이용해 Room 객체를 찾습니다. 해당 Room이 없으면 NotFoundRoomException 예외를 발생시킵니다.
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        // room 객체를 삭제합니다.
        roomRepository.delete(room);
    }

    // 방 나가기 기능
    public void leaveRoom(Long roomNum, Long userId) {
        // roomNum을 이용해 Room 객체를 찾습니다. 해당 Room이 없으면 NotFoundRoomException 예외를 발생시킵니다.
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        // userId를 이용해 User 객체를 찾습니다. 해당 User가 없으면 NotFoundRoomException 예외를 발생시킵니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + userId));

        // 방장의 ID와 나가는 사용자의 ID가 같으면 방을 삭제합니다.
        if (room.getRoomMaster().getId().equals(userId)) {
            roomRepository.delete(room);
        } else {
            // 방에 있는 사용자의 수를 감소시킵니다. userCount가 0 이상일 때만 감소시킵니다.
            int userCount = room.getRoomUserCount() != null ? room.getRoomUserCount() : 0;
            if (userCount > 0) {
                room.setRoomUserCount(userCount - 1);
            }
            // 참여자 목록에서 해당 user를 제거합니다.
            room.getParticipants().remove(user);
            // room 객체를 저장합니다.
            roomRepository.save(room);
        }
    }

    // 방 입장 기능
    public void joinRoom(Long roomNum, Long userId) {
        // roomNum을 이용해 Room 객체를 찾습니다. 해당 Room이 없으면 NotFoundRoomException 예외를 발생시킵니다.
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        // userId를 이용해 User 객체를 찾습니다. 해당 User가 없으면 NotFoundRoomException 예외를 발생시킵니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + userId));

        // 방에 있는 사용자의 수를 증가시킵니다.
        int userCount = room.getRoomUserCount() != null ? room.getRoomUserCount() : 0;
        room.setRoomUserCount(userCount + 1);

        // 참여자 목록에 해당 user를 추가합니다.
        room.getParticipants().add(user);
        // room 객체를 저장합니다.
        roomRepository.save(room);
    }

    //참여자 수 총합 기능
    public Long getTotalUserCountByRoomContent(String roomContent) {
        Long totalUserCount = roomRepository.getTotalUserCountByRoomContent(roomContent);
        if (totalUserCount == null) {
            throw new NotFoundRoomException("해당 내용에 대한 방을 찾을 수 없습니다: " + roomContent);
        }
        return totalUserCount;
    }
}