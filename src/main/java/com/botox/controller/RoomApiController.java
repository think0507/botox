package com.botox.controller;

import com.botox.constant.RoomStatus;
import com.botox.constant.RoomType;
import com.botox.domain.Room;
import com.botox.exception.NotFoundRoomException;
import com.botox.service.RoomService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.botox.domain.User;


@RestController // 컨트롤러를 사용할 때는 항상 RestController 사용
@RequiredArgsConstructor
@RequestMapping("/api") // 8080/api 형태로 매핑됨
@Slf4j
public class RoomApiController {
    private final RoomService roomService;

    // 게임 종류에 따른 방 목록 조회 기능
    @GetMapping("/rooms/{roomContent}")
    public ResponseForm<List<RoomForm>> getAllRoomListApi(@PathVariable String roomContent){
        try {
            List<RoomForm> roomForms = new ArrayList<>();
            List<Room> rooms = roomService.getAllRoomByContent(roomContent);

            for (Room room : rooms){
                RoomForm roomForm = convertRoomForm(room);
                roomForms.add(roomForm);
            }

            return new ResponseForm<>(HttpStatus.OK, roomForms, "OK");
        } catch (NotFoundRoomException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        } catch (Exception e) {
            log.info("error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예기치 않은 오류가 발생했습니다.");
        }
    }

    // 방 추가 기능
    @PostMapping("/rooms")
    public ResponseForm<RoomForm> createRoom(@RequestBody RoomForm roomForm) {
        System.out.println("Received roomMasterId: " + roomForm.getRoomMasterId());
        Room room = roomService.saveRoom(roomForm);
        RoomForm createdRoomForm = convertRoomForm(room);
        System.out.println("Converted roomMasterId: " + createdRoomForm.getRoomMasterId());
        return new ResponseForm<>(HttpStatus.CREATED, createdRoomForm, "방 생성을 완료했습니다.");
    }

    // 방 수정 기능
    @PutMapping("/rooms/{roomNum}")
    public ResponseForm<RoomForm> updateRoom(@PathVariable Long roomNum, @RequestBody RoomForm roomForm) {
        try {
            // 입력받은 데이터를 사용하여 방 정보 업데이트
            Room updateRoom = roomService.updateRoom(roomNum, roomForm);
            RoomForm updatedRoomForm = convertRoomForm(updateRoom);
            return new ResponseForm<>(HttpStatus.OK, updatedRoomForm, "방 수정을 완료했습니다.");
        } catch (NotFoundRoomException e) {
            // 방을 찾을 수 없을 때 발생하는 예외 처리
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (IllegalArgumentException e) {
            // 잘못된 인자가 전달되었을 때 발생하는 예외 처리
            log.error("Illegal argument error", e);
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, "잘못된 입력이 있습니다.");
        } catch (Exception e) {
            // 예기치 않은 오류가 발생했을 때 처리
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예기치 않은 오류가 발생했습니다.");
        }
    }

    // 방 삭제 기능
    @DeleteMapping("/rooms/{roomNum}")
    public ResponseForm<Void> deleteRoom(@PathVariable Long roomNum) {
        try {
            roomService.deleteRoom(roomNum);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "방 삭제를 완료했습니다.");
        } catch (NotFoundRoomException e) {
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예기치 않은 오류가 발생했습니다.");
        }
    }

    // 방 나가기 기능
    @PostMapping("/rooms/{roomNum}/leave")
    public ResponseForm<Void> leaveRoom(@PathVariable Long roomNum, @RequestBody LeaveRoomForm leaveRoomForm) {
        try {
            roomService.leaveRoom(roomNum, leaveRoomForm.getUserId());
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "방 나가기를 완료했습니다.");
        } catch (NotFoundRoomException e) {
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예기치 않은 오류가 발생했습니다.");
        }
    }

    // 방 입장 기능
    @PostMapping("/rooms/{roomNum}/join")
    public ResponseForm<Void> joinRoom(@PathVariable Long roomNum, @RequestBody JoinRoomForm joinRoomForm) {
        try {
            roomService.joinRoom(roomNum, joinRoomForm.getUserId());
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "방 입장을 완료했습니다.");
        } catch (NotFoundRoomException e) {
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예기치 않은 오류가 발생했습니다.");
        }
    }


    private RoomForm convertRoomForm(Room room) {
        return RoomForm.builder()
                .roomNum(room.getRoomNum())
                .roomTitle(room.getRoomTitle())
                .roomContent(room.getRoomContent())
                .roomType(room.getRoomType())
                .gameName(room.getGameName())
                .roomMasterId(room.getRoomMasterId() != null ? Long.valueOf(room.getRoomMasterId()) : null)
                .roomStatus(room.getRoomStatus())
                .roomCapacityLimit(room.getRoomCapacityLimit())
                .roomUpdateTime(Timestamp.valueOf(room.getRoomUpdateTime()))
                .roomCreateAt(Timestamp.valueOf(room.getRoomCreateAt()))
                .roomUserCount(room.getRoomUserCount())
                .participantIds(room.getParticipants().stream().map(User::getId).collect(Collectors.toList()))
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class RoomForm {
        private Long roomNum;
        private String roomTitle;
        private String roomContent;
        private RoomType roomType;
        private String gameName;
        private Long roomMasterId;
        private RoomStatus roomStatus;
        private Integer roomCapacityLimit;
        private Timestamp roomUpdateTime;
        private Timestamp roomCreateAt;
        private  Integer roomUserCount;
        private List<Long> participantIds; // 참가자 ID 목록 추가

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinRoomForm {
        private Long userId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveRoomForm {
        private Long userId;
    }
}