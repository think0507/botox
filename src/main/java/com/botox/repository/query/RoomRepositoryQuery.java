package com.botox.repository.query;

import com.botox.domain.Room;

import java.util.List;

public interface RoomRepositoryQuery {
    List<Room> getRoomByContent(String roomContent);

    List<Room> findByRoomContent(String roomContent);
    Long getTotalUserCountByRoomContent(String roomContent); // 총 유저 수를 계산하는 메서드 추가

}