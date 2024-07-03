package com.botox.repository.query;

import com.botox.domain.Room;

import java.util.List;

public interface RoomRepositoryQuery {
    List<Room> getRoomByContent(String roomContent);
}