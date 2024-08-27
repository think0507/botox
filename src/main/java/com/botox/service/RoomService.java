package com.botox.service;

import com.botox.config.jwt.TokenProvider;
import com.botox.controller.RoomApiController;
import com.botox.domain.Room;
import com.botox.domain.RoomParticipant;
import com.botox.domain.User;
import com.botox.exception.NotFoundRoomException;
import com.botox.repository.RoomRepository;
import com.botox.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.plaf.basic.BasicTreeUI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.UUID;

import static org.kurento.jsonrpc.client.JsonRpcClient.log;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

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
        room.getParticipants().add(new RoomParticipant(room, roomMaster, LocalDateTime.now()));
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

    //60초마다 비어있는 방 지우는 메서드
    @Scheduled(fixedDelay = 60000) // 매 60초마다 실행
    @Transactional
    public void checkAndDeleteEmptyRooms() {
        // UserCount가 0인 방을 찾는 쿼리를 실행합니다.
        List<Room> emptyRooms = roomRepository.findByUserCount(0);

        // 찾은 빈 방들을 삭제합니다.
        for (Room room : emptyRooms) {
            if(room.getRoomMaster()== null) {
                roomRepository.delete(room);
            }
        }
    }
    // 방 나가기 기능
    @Transactional
    public void leaveRoom(Long roomNum, Long userId) {
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + userId));

        // 방장이 나가는 경우
        if (room.getRoomMaster().getId().equals(userId)) {
            room.getParticipants().removeIf(participant -> participant.getUser().getId().equals(userId));

            if (!room.getParticipants().isEmpty()) {
                // 새 방장 설정
                User newMaster = room.getParticipants().stream()
                        .min(Comparator.comparing(RoomParticipant::getEntryTime))
                        .map(RoomParticipant::getUser)
                        .orElseThrow(() -> new NotFoundRoomException("참가자가 존재하지 않습니다."));
                room.setRoomMaster(newMaster);

                // 새로운 방장을 참여자 명단에서 제거
                room.getParticipants().removeIf(participant -> participant.getUser().getId().equals(newMaster.getId()));
                room.setRoomUserCount(room.getRoomUserCount()-1);
            } else {
                // 방에 참가자가 없으면 방 삭제
                roomRepository.delete(room);
                return;
            }
        } else {
            // 방장이 아닌 사용자가 나가는 경우
            int userCount = room.getRoomUserCount() != null ? room.getRoomUserCount() : 0;
            if (userCount > 0) {
                room.setRoomUserCount(userCount - 1);
            }
            room.getParticipants().removeIf(participant -> participant.getUser().getId().equals(userId));
        }

        roomRepository.save(room);
    }

    // 방 입장 기능
    @Transactional
    public void joinRoom(Long roomNum, Long userId, String password) {
        String lockKey = LOCK_PREFIX + roomNum;

        // 잠금 시도
        boolean lockAcquired = redisLockService.acquireLock(lockKey, Duration.ofSeconds(1));

        if (!lockAcquired) {
            throw new IllegalStateException("다른 사용자가 먼저 방에 접근했습니다.");
        }

        try {
            Room room = roomRepository.findById(roomNum)
                    .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + userId));

            if (room.getRoomPassword() != null && !room.getRoomPassword().isEmpty() && !room.getRoomPassword().equals(password)) {
                throw new IllegalArgumentException("잘못된 비밀번호입니다.");
            }

            int userCount = room.getRoomUserCount() != null ? room.getRoomUserCount() : 0;
            if (userCount >= room.getRoomCapacityLimit()) {
                throw new IllegalStateException("방의 최대 인원 수를 초과했습니다.");
            }
            room.setRoomUserCount(userCount + 1);

            RoomParticipant participant = new RoomParticipant(room, user, LocalDateTime.now());
            room.getParticipants().add(participant);

            roomRepository.save(room);

        } finally {
            // 잠금 해제
            redisLockService.releaseLock(lockKey);
        }
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
            selectedRoom.getParticipants().add(new RoomParticipant());

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
        selectedRoom.getParticipants().add(new RoomParticipant());

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


    //특정 방 조회
    public Room getRoomById(Long roomNum){
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(()->new NotFoundRoomException("해당 방을 찾을 수 없습니다: " +roomNum));

        return room;
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

    //참여자 수 총합 기능
    public Long getTotalUserCountByRoomContent(String roomContent) {
        Long totalUserCount = roomRepository.getTotalUserCountByRoomContent(roomContent);
        if (totalUserCount == null) {
            throw new NotFoundRoomException("해당 내용에 대한 방을 찾을 수 없습니다: " + roomContent);
        }
        return totalUserCount;
    }

    // 사용자 강퇴 기능
    @Transactional
    public void kickUser(Long roomNum, Long roomMasterId, Long userIdToKick) {
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        // 방장이 맞는지 확인
        if (!room.getRoomMaster().getId().equals(roomMasterId)) {
            throw new IllegalArgumentException("강퇴 권한이 없습니다.");
        }

        userRepository.findById(userIdToKick)
                .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + userIdToKick));

        // 해당 사용자가 방에 있는지 확인
        boolean isParticipant = room.getParticipants().stream()
                .anyMatch(participant -> participant.getUser().getId().equals(userIdToKick));

        if (!isParticipant) {
            throw new IllegalArgumentException("해당 사용자는 방에 없습니다.");
        }

        // 사용자 강퇴
        room.getParticipants().removeIf(participant -> participant.getUser().getId().equals(userIdToKick));
        room.setRoomUserCount(room.getRoomUserCount() - 1);

        roomRepository.save(room);
    }

    // 방장 권한 위임 기능
    @Transactional
    public void transferRoomMaster(Long roomNum, Long currentMasterId, Long newMasterId) {
        // roomNum을 이용해 Room 객체를 찾습니다. 해당 Room이 없으면 NotFoundRoomException 예외를 발생시킵니다.
        Room room = roomRepository.findById(roomNum)
                .orElseThrow(() -> new NotFoundRoomException("해당 방을 찾을 수 없습니다: " + roomNum));

        // 현재 방장의 ID가 주어진 currentMasterId와 일치하는지 확인합니다. 일치하지 않으면 IllegalArgumentException 예외를 발생시킵니다.
        if (!room.getRoomMaster().getId().equals(currentMasterId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // newMasterId를 이용해 User 객체를 찾습니다. 해당 User가 없으면 NotFoundRoomException 예외를 발생시킵니다.
        User newMaster = userRepository.findById(newMasterId)
                .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + newMasterId));

        // 새로운 방장이 방의 참가자인지 확인합니다.
        boolean isNewMasterParticipant = room.getParticipants().stream()
                .anyMatch(participant -> participant.getUser().getId().equals(newMasterId));

        // 새로운 방장이 참가자가 아니면 IllegalArgumentException 예외를 발생시킵니다.
        if (!isNewMasterParticipant) {
            throw new IllegalArgumentException("새로운 방장은 방의 참가자여야 합니다.");
        }

        // currentMasterId를 이용해 현재 방장 User 객체를 찾습니다. 해당 User가 없으면 NotFoundRoomException 예외를 발생시킵니다.
        User currentMaster = userRepository.findById(currentMasterId)
                .orElseThrow(() -> new NotFoundRoomException("해당 사용자를 찾을 수 없습니다: " + currentMasterId));

        // 현재 방장이 참가자 목록에 있는지 확인합니다.
        boolean isCurrentMasterParticipant = room.getParticipants().stream()
                .anyMatch(participant -> participant.getUser().getId().equals(currentMasterId));

        // 현재 방장이 참가자 목록에 없으면 추가합니다.
        if (!isCurrentMasterParticipant) {
            room.getParticipants().add(new RoomParticipant(room, currentMaster, LocalDateTime.now()));
        }

        // 새 방장이 참가자 목록에 없으면 추가합니다.
        if (!isNewMasterParticipant) {
            room.getParticipants().add(new RoomParticipant(room, newMaster, LocalDateTime.now()));
        }

        // 새로운 방장은 참가자 목록에서 제거
        room.getParticipants().removeIf(participant -> participant.getUser().getId().equals(newMasterId));


        // 방의 새로운 방장을 설정합니다.
        room.setRoomMaster(newMaster);
        // 변경된 Room 객체를 저장합니다.
        roomRepository.save(room);
    }
}

