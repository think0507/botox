package com.botox.repository;

import com.botox.domain.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByAcceptedUserIdAndRequestedUserId(Long acceptedUserId, Long requestedUserId);
    List<Friendship> findByAcceptedUserIdOrRequestedUserId(Long acceptedUserId, Long requestedUserId);
}
