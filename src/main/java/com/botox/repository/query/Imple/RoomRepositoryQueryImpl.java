package com.botox.repository.query.Imple;

import com.botox.domain.Room;
import com.botox.repository.query.RoomRepositoryQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoomRepositoryQueryImpl implements RoomRepositoryQuery {
    private final EntityManager em;

    @Override
    public List<Room> getRoomByContent(String roomContent) {
        String jpql = "SELECT r FROM Room r WHERE r.roomContent = :roomContent";
        return em.createQuery(jpql, Room.class)
                .setParameter("roomContent", roomContent)
                .getResultList();
    }
}