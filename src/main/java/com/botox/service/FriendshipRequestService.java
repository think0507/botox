package com.botox.service;

import com.botox.constant.RequestStatus;
import com.botox.domain.*;
import com.botox.repository.FriendshipRequestRepository;
import com.botox.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipRequestService {

    private final FriendshipRequestRepository friendshipRequestRepository;
    private final UserRepository userRepository;

    public FriendshipRequestDTO sendFriendRequest(Long senderId, Long receiverId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        FriendshipRequest friendshipRequest = new FriendshipRequest();
        friendshipRequest.setSender(sender);
        friendshipRequest.setReceiver(receiver);
        friendshipRequest.setRequestTime(LocalDateTime.now());
        friendshipRequest.setStatus(RequestStatus.PENDING);

        FriendshipRequest savedRequest = friendshipRequestRepository.save(friendshipRequest);

        return convertToDTO(savedRequest);
    }

    private FriendshipRequestDTO convertToDTO(FriendshipRequest friendshipRequest) {
        FriendshipRequestDTO dto = new FriendshipRequestDTO();
        dto.setRequestId(friendshipRequest.getRequestId());
        dto.setSenderId(friendshipRequest.getSender().getId());
        dto.setReceiverId(friendshipRequest.getReceiver().getId());
        dto.setRequestTime(friendshipRequest.getRequestTime());
        dto.setStatus(friendshipRequest.getStatus());
        return dto;
    }




    public FriendshipRequest respondToFriendRequest(Long requestId, String status) {
        FriendshipRequest request = friendshipRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(RequestStatus.valueOf(status.toUpperCase()));

        if (request.getStatus() == RequestStatus.ACCEPTED) {
            Friendship friendship = new Friendship();
            friendship.setAcceptedUser(request.getReceiver());
            friendship.setRequestedUser(request.getSender());
            // Save friendship to FriendshipRepository here
        }

        return friendshipRequestRepository.save(request);
    }

    public List<FriendshipRequest> getPendingRequests(Long userId) {
        return friendshipRequestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING);
    }

}
