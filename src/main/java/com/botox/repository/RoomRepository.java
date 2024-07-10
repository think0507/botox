package com.botox.repository;

import com.botox.domain.Room;
import com.botox.repository.query.RoomRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room,Long>, RoomRepositoryQuery {
    List<Room> findByRoomContent(String roomContent);
}
