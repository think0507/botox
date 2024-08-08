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
import org.springframework.transaction.annotation.Transactional;

import javax.swing.plaf.basic.BasicTreeUI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final RedisLockService redisLockService;
    private static final String LOCK_PREFIX = "ROOM_LOCK";

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
        room.setRoomPassword(roomForm.getRoomPassword());
        room.setRoomCapacityLimit(roomForm.getRoomCapacityLimit());
        room.setRoomUpdateTime(roomForm.getRoomUpdateTime().toLocalDateTime());
        room.setRoomCreateAt(LocalDateTime.now());

        // 방에 있는 사용자의 수를 1로 설정 (방장 포함)
        room.setRoomUserCount(1);

        // 방장 추가
        room.getParticipants().add(roomMaster);

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
    @Transactional
    public void joinRoom(Long roomNum, Long userId, String password) {
        // roomNum을 이용해 Room 객체를 찾습니다. 해당 Room이 없으면 NotFoundRoomException 예외를 발생시킵니다.
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        // userId를 이용해 User 객체를 찾습니다. 해당 User가 없으면 NotFoundRoomException 예외를 발생시킵니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + userId));

        // 비밀번호 검증
        if (room.getRoomPassword() != null && !room.getRoomPassword().isEmpty() && !room.getRoomPassword().equals(password)) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        // 방에 있는 사용자의 수를 증가시킵니다.
        int userCount = room.getRoomUserCount() != null ? room.getRoomUserCount() : 0;
        room.setRoomUserCount(userCount + 1);

        // 참여자 목록에 해당 user를 추가합니다.
        room.getParticipants().add(user);
        // room 객체를 저장합니다.
        roomRepository.save(room);
    }

    // 기존 방 입장 기능 (비밀번호 없이)
    @Transactional
    public void joinRoom(Long roomNum, Long userId) {
        joinRoom(roomNum, userId, null);
    }

    // 빠른 방 입장
    @Transactional
    public void enterRoom(String roomContent, Long userId) {
        String lockKey = LOCK_PREFIX + userId;

        // 잠금 시도
        boolean lockAcquired = redisLockService.acquireLock(lockKey, Duration.ofSeconds(1));
        if (!lockAcquired) {
            // 잠금을 획득하지 못한 경우 다른 방으로 이동 시도
            enterRoomFallback(roomContent, userId);
            return; // 다른 방으로 이동한 경우 종료
        }

        try {
            // 사용자 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundRoomException("사용자를 찾을 수 없습니다: " + userId));

            // 해당 roomContent로 방을 조회하고 입장
            Room selectedRoom = findAvailableRoom(roomContent);
            if (selectedRoom == null) {
                throw new IllegalStateException("입장할 수 있는 방이 없습니다.");
            }

            // 방에 있는 사용자의 수를 증가시킵니다.
            int userCount = selectedRoom.getRoomUserCount() != null ? selectedRoom.getRoomUserCount() : 0;
            selectedRoom.setRoomUserCount(userCount + 1);

            // 참여자 목록에 해당 user를 추가합니다.
            selectedRoom.getParticipants().add(user);

            // selectedRoom 객체를 저장합니다.
            roomRepository.save(selectedRoom);

        } finally {
            // 잠금 해제
            redisLockService.releaseLock(lockKey);
        }
    }

    // 다른 사용자가 먼저 방에 접근할 경우 다른 방으로 자동으로 접속하는 기능
    private void enterRoomFallback(String roomContent, Long userId) {
        Room selectedRoom = findAvailableRoom(roomContent);
        if (selectedRoom == null) {
            throw new IllegalStateException("입장할 수 있는 방이 없습니다.");
        }

        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundRoomException("사용자를 찾을 수 없습니다: " + userId));

        // 방에 있는 사용자의 수를 증가시킵니다.
        int userCount = selectedRoom.getRoomUserCount() != null ? selectedRoom.getRoomUserCount() : 0;
        selectedRoom.setRoomUserCount(userCount + 1);

        // 참여자 목록에 해당 user를 추가합니다.
        selectedRoom.getParticipants().add(user);

        // selectedRoom 객체를 저장합니다.
        roomRepository.save(selectedRoom);
    }

    // 비밀번호가 설정되지 않았고, 최대 인원에 도달하지 않은 방을 조회합니다.
    private Room findAvailableRoom(String roomContent) {
        List<Room> availableRooms = roomRepository.findByRoomContent(roomContent).stream()
                .filter(room -> room.getRoomPassword() == null) // 비밀번호가 없음
                .filter(room -> room.getRoomUserCount() != null &&
                        room.getRoomUserCount() < room.getRoomCapacityLimit())
                .collect(Collectors.toList());

        if (availableRooms.isEmpty()) {
            return null; // 입장할 수 있는 방이 없음
        }

        // 필터링된 방 중 랜덤으로 하나 선택
        return availableRooms.get(new Random().nextInt(availableRooms.size()));
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