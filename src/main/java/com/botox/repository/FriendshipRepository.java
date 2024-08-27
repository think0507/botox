package com.botox.repository;

import com.botox.domain.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE (f.acceptedUser.userNickname = :nickname1 AND f.requestedUser.userNickname = :nickname2)" +
            "OR (f.acceptedUser.userNickname = :nickname2 AND f.requestedUser.userNickname = :nickname1)")
    List<Friendship> findFriendshipBetweenUsers(String nickname1, String nickname2);

    List<Friendship> findByAcceptedUser_UserNicknameOrRequestedUser_UserNickname(String acceptedNickname, String requestedNickname);
}