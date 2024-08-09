package com.botox.service;

import com.botox.config.jwt.TokenProvider;
import com.botox.controller.ResponseForm;
import com.botox.controller.RoomApiController;
import com.botox.domain.Room;
import com.botox.domain.User;
import com.botox.exception.NotFoundRoomException;
import com.botox.repository.RoomRepository;
import com.botox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.kurento.jsonrpc.client.JsonRpcClient.log;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + userId));

        if (room.getRoomMaster().getId().equals(userId)) {
            roomRepository.delete(room);
        } else {
            int userCount = room.getRoomUserCount() != null ? room.getRoomUserCount() : 0;
            if (userCount > 0) {
                room.setRoomUserCount(userCount - 1);
            }
            room.getParticipants().remove(user);
            roomRepository.save(room);
        }
    }

    // 방 입장 기능
    public void joinRoom(Long roomNum, Long userId) {
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + userId));

        int userCount = room.getRoomUserCount() != null ? room.getRoomUserCount() : 0;
        room.setRoomUserCount(userCount + 1);

        room.getParticipants().add(user);
        roomRepository.save(room);
    }

    // 초대 링크 생성 기능
    public String generateInviteLink(Long roomNum) {
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        if (room.getInviteCode() == null) {
            String inviteCode = generateUniqueInviteCode();
            room.setInviteCode(inviteCode);
            roomRepository.save(room);
        }

        return "/api/rooms/guest-join/" + room.getInviteCode();
    }

    // 초대 링크 중복 방지 기능
    private String generateUniqueInviteCode() {
        String inviteCode;
        do {
            inviteCode = UUID.randomUUID().toString().substring(0, 8);
        } while (roomRepository.findByInviteCode(inviteCode).isPresent());

        return inviteCode;
    }

    @Transactional
    public String joinRoomAsGuest(String inviteCode) {
        Room room = roomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new NotFoundRoomException("유효하지 않은 초대 코드입니다."));

        if (room.getRoomUserCount() >= room.getRoomCapacityLimit()) {
            throw new IllegalStateException("방이 가득 찼습니다.");
        }

        // 방 인원 카운트 증가
        room.setRoomUserCount(room.getRoomUserCount() + 1);
        roomRepository.save(room);

        // 게스트용 JWT 생성
        String guestToken = tokenProvider.generateGuestToken(room.getRoomNum());

        return guestToken;
    }


    // 게스트 방 퇴장 기능
    @Transactional
    public void removeGuestFromRoom(Long roomNum, String token) {
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        // 방 인원 수 감소
        if (room.getRoomUserCount() > 0) {
            room.setRoomUserCount(room.getRoomUserCount() - 1);
        }

        // 방 정보 업데이트
        roomRepository.save(room);

        // 토큰에서 UUID 추출
        String uuid = tokenProvider.getUUIDFromGuestToken(token);

        // Redis 키 생성
        String redisKey = "GUEST_TOKEN:" + roomNum + ":" + uuid;
        log.info("Attempting to delete Redis key: {}", redisKey); // 디버깅 로그 추가

        Boolean result = redisTemplate.delete(redisKey);

        if (Boolean.FALSE.equals(result)) {
            log.warn("Redis key {} was not deleted or did not exist", redisKey);
            throw new IllegalStateException("게스트 토큰 삭제에 실패했습니다.");
        }
    }
}
