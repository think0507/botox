package com.botox.repository;

import com.botox.domain.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {


    @Query("SELECT f FROM Friendship f WHERE (f.acceptedUser.id = :userId1 AND f.requestedUser.id = :userId2)" +
            "OR (f.acceptedUser.id = :userId2 AND f.requestedUser.id = :userId1)")
    List<Friendship> findFriendshipBetweenUsers(Long userId1, Long userId2);
    List<Friendship> findByAcceptedUserIdOrRequestedUserId(Long acceptedUserId, Long requestedUserId);
}
