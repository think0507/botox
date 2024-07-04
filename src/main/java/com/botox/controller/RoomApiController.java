package com.botox.controller;

import com.botox.constant.RoomStatus;
import com.botox.constant.RoomType;
import com.botox.domain.Room;
import com.botox.exception.NotFoundRoomException;
import com.botox.service.RoomService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@RestController //컨트롤러를 사용할때는 항상 RestController 사용
@RequiredArgsConstructor
@RequestMapping("/api") //8080/api 형태로 매핑됨
@Slf4j
public class RoomApiController {
    private final RoomService roomService;

    //게임 종류에 따른 방 목록 조회 기능
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
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "An unexpected error occurred");
        }
    }

    //방 추가 기능
    @PostMapping("/rooms")
    public ResponseForm<RoomForm> createRoom(@RequestBody RoomForm roomForm) {
        Room room = new Room();
        room.setRoomTitle(roomForm.getRoomTitle());
        room.setRoomContent(roomForm.getRoomContent());
        room.setRoomType(roomForm.getRoomType());
        room.setGameName(roomForm.getGameName());
        // roomMasterId를 이용해 User 객체 설정 (이 부분은 추가 로직 필요)
        room.setRoomStatus(roomForm.getRoomStatus());
        room.setRoomCapacityLimit(roomForm.getRoomCapacityLimit());
        room.setRoomUpdateTime(roomForm.getRoomUpdateTime().toLocalDateTime());
        room.setRoomCreateAt(roomForm.getRoomCreateAt().toLocalDateTime());
        roomService.saveRoom(room);

        RoomForm createdRoomForm = convertRoomForm(room);
        return new ResponseForm<>(HttpStatus.CREATED, createdRoomForm, "Room created successfully");
    }



    private RoomForm convertRoomForm(Room room) {
        return RoomForm.builder()
                .roomNum(room.getRoomNum())
                .roomTitle(room.getRoomTitle())
                .roomContent(room.getRoomContent())
                .roomType(room.getRoomType())
                .gameName(room.getGameName())
                .roomMasterId(room.getRoomMasterId())
                .roomStatus(room.getRoomStatus())
                .roomCapacityLimit(room.getRoomCapacityLimit())
                .roomUpdateTime(Timestamp.valueOf(room.getRoomUpdateTime()))
                .roomCreateAt(Timestamp.valueOf(room.getRoomCreateAt()))
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
        private Timestamp roomUpdateTime; //이거 찾아봐야 됨 LocalDatatime이랑 차이점
        private Timestamp roomCreateAt;
}


}
