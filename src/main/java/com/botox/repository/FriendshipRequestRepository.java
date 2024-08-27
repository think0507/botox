package com.botox.repository;

import com.botox.domain.FriendshipRequest;
import com.botox.constant.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long> {
    List<FriendshipRequest> findBySender_UserNicknameAndStatus(String senderNickname, RequestStatus status);
    List<FriendshipRequest> findByReceiver_UserNicknameAndStatus(String receiverNickname, RequestStatus status);
    Optional<FriendshipRequest> findBySender_UserNicknameAndReceiver_UserNicknameAndStatus(String senderNickname, String receiverNickname, RequestStatus status);
}