package com.botox.repository;

import com.botox.domain.FriendshipRequest;
import com.botox.constant.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long> {
    List<FriendshipRequest> findByReceiverIdAndStatus(Long receiverId, RequestStatus status);
}
