package com.botox.controller;

import com.botox.constant.RoomStatus;
import com.botox.constant.RoomType;
import com.botox.domain.Room;
import com.botox.exception.NotFoundRoomException;
import com.botox.logger.RoomLogger;
import com.botox.service.RoomService;
import jakarta.servlet.http.HttpServletRequest;
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
    // RoomForm에 담고, ResponseForm에 담아 출력
    public ResponseForm<List<RoomForm>> getAllRoomListApi(@PathVariable String roomContent){
        try {
            // roomForms는 모든 Room 객체를 RoomForm 객체로 변환하여 저장할 리스트
            List<RoomForm> roomForms = new ArrayList<>();

            // roomContent를 받아 roomService에서 getAllRoomByContent를 수행해 List<Room> 타입의 변수 rooms에 넣음
            List<Room> rooms = roomService.getAllRoomByContent(roomContent);

            // rooms 리스트 안에 있는 room 객체들을 돌면서 형태를 roomForm -> convertRoomForm으로 바꿔줌
            for (Room room : rooms){
                // convertRoomForm 메서드를 호출하여 현재 Room 객체를 RoomForm 객체로 변환
                RoomForm roomForm = convertRoomForm(room);
                // 변환된 RoomForm 객체를 roomForms 리스트에 추가
                roomForms.add(roomForm);
            }

            // 변환된 RoomForm 리스트를 포함한 ResponseForm 객체를 반환
            return new ResponseForm<>(HttpStatus.OK, roomForms, "OK");
        } catch (NotFoundRoomException e) {
            // 방을 찾을 수 없을 때 NO_CONTENT 상태와 함께 메시지를 반환
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        } catch (Exception e) {
            // 기타 예기치 않은 오류 발생 시 INTERNAL_SERVER_ERROR 상태와 함께 메시지를 반환
            log.info("error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예기치 않은 오류가 발생했습니다.");
        }
    }

    // 방 추가 기능
    @PostMapping("/rooms")
    public ResponseForm<RoomForm> createRoom(@RequestBody RoomForm roomForm) {
        // roomMasterId를 출력하여 디버깅
        System.out.println("Received roomMasterId: " + roomForm.getRoomMasterId());

        // roomService에서 roomForm을 매개변수로 받아 saveRoom 기능을 수행하고 결과 값을 room에 저장
        Room room = roomService.saveRoom(roomForm);

        // 해당 결과 값이 저장된 room 객체를 convertRoomForm으로 변환하고 RoomForm 객체의 CreateRoomForm에 저장
        RoomForm createdRoomForm = convertRoomForm(room);

        // 변환된 roomMasterId를 출력하여 디버깅
        System.out.println("Converted roomMasterId: " + createdRoomForm.getRoomMasterId());

        // 생성된 RoomForm 객체를 포함한 ResponseForm 객체를 반환
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
    //deleteRoom 이라는 메서드로 roomNum 매개변수를 받아 ResponseForm으로 반환.
    public ResponseForm<Void> deleteRoom(@PathVariable Long roomNum) {
        try {
            //roomService의 deleteRoom 메서드를 사용(매개변수는 roomNum)
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
    public ResponseForm<Void> leaveRoom(@PathVariable Long roomNum, @RequestBody LeaveRoomForm leaveRoomForm, HttpServletRequest request) {
        try {
            roomService.leaveRoom(roomNum, leaveRoomForm.getUserId());
            RoomLogger.RoomLog("leave", roomNum, null, leaveRoomForm.getUserId(), request);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "방 나가기를 완료했습니다.");
        } catch (NotFoundRoomException e) {
            RoomLogger.RoomLog("leave", roomNum, null, leaveRoomForm.getUserId(), request);
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Exception e) {
            RoomLogger.RoomLog("leave", roomNum, null, leaveRoomForm.getUserId(), request);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예기치 않은 오류가 발생했습니다.");
        }
    }



    // 방 입장 기능
    @PostMapping("/rooms/{roomNum}/join")
    public ResponseForm<Void> joinRoom(@PathVariable Long roomNum, @RequestBody JoinRoomForm joinRoomForm, HttpServletRequest request) {
        try {
            roomService.joinRoom(roomNum, joinRoomForm.getUserId(), joinRoomForm.getPassword());
            RoomLogger.RoomLog("join", roomNum, null, joinRoomForm.getUserId(), request);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "방 입장을 완료했습니다.");
        } catch (NotFoundRoomException e) {
            RoomLogger.RoomLog("join", roomNum, null, joinRoomForm.getUserId(), request);
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (IllegalArgumentException e) {
            RoomLogger.RoomLog("join", roomNum, null, joinRoomForm.getUserId(), request);
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "잘못된 비밀번호입니다.");
        } catch (Exception e) {
            RoomLogger.RoomLog("join", roomNum, null, joinRoomForm.getUserId(), request);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예기치 않은 오류가 발생했습니다.");
        }
    }

    // 빠른 방 입장 기능
    @PostMapping("/rooms/{roomContent}/enter")
    public ResponseForm<Void> enterRoom(@PathVariable String roomContent, @RequestBody EnterRoomForm enterRoomForm, HttpServletRequest request) {
        try {
            roomService.enterRoom(roomContent, enterRoomForm.getUserId());
            RoomLogger.RoomLog("enter", null, roomContent, enterRoomForm.getUserId(), request);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "빠른 방 입장을 완료했습니다.");
        } catch (NotFoundRoomException e) {
            RoomLogger.RoomLog("enter", null, roomContent, enterRoomForm.getUserId(), request);
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (IllegalStateException e) {
            RoomLogger.RoomLog("enter", null, roomContent, enterRoomForm.getUserId(), request);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        } catch (Exception e) {
            RoomLogger.RoomLog("enter", null, roomContent, enterRoomForm.getUserId(), request);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예기치 않은 오류가 발생했습니다.");
        }
    }

    //게임별로 유저 수 측정하는 API
    @GetMapping("/rooms/{roomContent}/count")
    public ResponseForm<Long> getTotalUserCountByRoomContent(@PathVariable String roomContent) {
        try {
            Long totalUserCount = roomService.getTotalUserCountByRoomContent(roomContent);
            return new ResponseForm<>(HttpStatus.OK, totalUserCount, "OK");
        } catch (NotFoundRoomException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예기치 않은 오류가 발생했습니다.");
        }
    }

    // 방 강퇴 기능
    @PostMapping("/rooms/{roomNum}/kick")
    public ResponseForm<Void> kickUser(@PathVariable Long roomNum, @RequestBody KickUserForm kickUserForm) {
        try {
            roomService.kickUser(roomNum, kickUserForm.getRoomMasterId(), kickUserForm.getUserIdToKick());
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "사용자를 강퇴했습니다.");
        } catch (NotFoundRoomException e) {
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (IllegalArgumentException e) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "강퇴 권한이 없습니다.");
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예기치 않은 오류가 발생했습니다.");
        }
    }

    // 방장 권한 위임 기능
    @PostMapping("/rooms/{roomNum}/transfer")
    public ResponseForm<Void> transferRoomMaster(@PathVariable Long roomNum, @RequestBody TransferMasterForm transferMasterForm) {
        try {
            roomService.transferRoomMaster(roomNum, transferMasterForm.getCurrentMasterId(), transferMasterForm.getNewMasterId());
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "방장 권한을 성공적으로 위임했습니다.");
        } catch (NotFoundRoomException e) {
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (IllegalArgumentException e) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "권한이 없습니다.");
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
                .roomPassword(room.getRoomPassword())
                .roomMasterId(room.getRoomMasterId() != null ? Long.valueOf(room.getRoomMasterId()) : null)
                .roomStatus(room.getRoomStatus())
                .roomCapacityLimit(room.getRoomCapacityLimit())
                .roomUpdateTime(Timestamp.valueOf(room.getRoomUpdateTime()))
                .roomCreateAt(Timestamp.valueOf(room.getRoomCreateAt()))
                .roomUserCount(room.getRoomUserCount())
                //.participantIds(room.getParticipants().stream().map(User::getId).collect(Collectors.toList()))
                //원래 room.getparticipantIds()가 List<user>가 아니라 List<RoomParticipant>를 반환하기 때문임
                //그래서 User 객체의 ID를 직접 가져오려면 RoomParticipant 객체를 통해 접근해야 함
                .participantIds(room.getParticipants().stream()
                        .map(participant -> participant.getUser().getId())
                        .collect(Collectors.toList()))

                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class RoomForm {
        private Long roomNum;
        private String roomTitle;
        private String roomContent;
        private String roomPassword;
        private RoomType roomType;
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
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KickUserForm {
        private Long roomMasterId;
        private Long userIdToKick;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferMasterForm {
        private Long currentMasterId;
        private Long newMasterId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveRoomForm {
        private Long userId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnterRoomForm {
        private Long userId;
    }
}