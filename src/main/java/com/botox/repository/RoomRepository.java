package com.botox.repository;

import com.botox.domain.Room;
import com.botox.repository.query.RoomRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room,Long>, RoomRepositoryQuery{
    List<Room> findByRoomContent(String roomContent);

    List<Room> findByUserCount(int roomUserCount);
    // 방 초대코드로 방 찾기
    Optional<Room> findByInviteCode(String inviteCode);
    Long getTotalUserCountByRoomContent(String roomContent); // 총 유저 수를 계산하는 메서드 추가

}
