package com.botox.repository;

import com.botox.domain.FriendshipRequest;
import com.botox.constant.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long> {
    List<FriendshipRequest> findBySenderIdAndStatus(Long senderId, RequestStatus status);
    List<FriendshipRequest> findByReceiverIdAndStatus(Long receiverId, RequestStatus status);
    Optional<FriendshipRequest> findBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, RequestStatus status);
}