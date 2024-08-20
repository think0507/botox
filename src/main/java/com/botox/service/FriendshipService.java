package com.botox.service;

import com.botox.constant.RequestStatus;
import com.botox.domain.Friendship;
import com.botox.domain.FriendshipRequest;
import com.botox.domain.FriendshipRequestCreateRequest;
import com.botox.domain.FriendshipRequestDTO;
import com.botox.domain.User;
import com.botox.repository.FriendshipRepository;
import com.botox.repository.FriendshipRequestRepository;
import com.botox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipRequestRepository friendshipRequestRepository;

    public FriendshipRequestDTO sendFriendRequest(FriendshipRequestCreateRequest request) {
        if (isAlreadyFriendsOrPending(request.getSenderId(), request.getReceiverId())) {
            throw new IllegalStateException("Friend request already sent or you are already friends");
        }

        User sender = userRepository.findById(request.getSenderId()).orElseThrow(() -> new IllegalStateException("Sender not found"));
        User receiver = userRepository.findById(request.getReceiverId()).orElseThrow(() -> new IllegalStateException("Receiver not found"));

        FriendshipRequest friendshipRequest = new FriendshipRequest();
        friendshipRequest.setSender(sender);
        friendshipRequest.setReceiver(receiver);
        friendshipRequest.setRequestTime(LocalDateTime.now());
        friendshipRequest.setStatus(RequestStatus.PENDING);

        friendshipRequestRepository.save(friendshipRequest);

        return convertToDTO(friendshipRequest);
    }

    public boolean isAlreadyFriendsOrPending(Long senderId, Long receiverId) {
        boolean alreadyFriends = isAlreadyFriends(senderId, receiverId);
        boolean pendingRequest = friendshipRequestRepository.findBySenderIdAndReceiverIdAndStatus(senderId, receiverId, RequestStatus.PENDING).isPresent()
                || friendshipRequestRepository.findBySenderIdAndReceiverIdAndStatus(receiverId, senderId, RequestStatus.PENDING).isPresent()
                || friendshipRequestRepository.findBySenderIdAndReceiverIdAndStatus(senderId, receiverId, RequestStatus.ACCEPTED).isPresent()
                || friendshipRequestRepository.findBySenderIdAndReceiverIdAndStatus(receiverId, senderId, RequestStatus.ACCEPTED).isPresent();

        return alreadyFriends || pendingRequest;
    }

    public boolean isAlreadyFriends(Long senderId, Long receiverId) {
        List<Friendship> friendships = friendshipRepository.findFriendshipBetweenUsers(senderId, receiverId);
        return !friendships.isEmpty();
    }

    public List<FriendshipRequestDTO> getPendingFriendRequests(Long userId) {
        List<FriendshipRequest> sentRequests = friendshipRequestRepository.findBySenderIdAndStatus(userId, RequestStatus.PENDING);
        List<FriendshipRequest> receivedRequests = friendshipRequestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING);

        List<FriendshipRequestDTO> pendingRequests = sentRequests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        pendingRequests.addAll(receivedRequests.stream()
                .map(this::convertToDTO)
                .toList());

        return pendingRequests;
    }

    public FriendshipRequestDTO acceptFriendRequest(Long requestId) {
        FriendshipRequest friendshipRequest = friendshipRequestRepository.findById(requestId).orElseThrow(() -> new IllegalStateException("Friend request not found"));
        friendshipRequest.setStatus(RequestStatus.ACCEPTED);
        friendshipRequestRepository.save(friendshipRequest);

        Friendship friendship = new Friendship();
        friendship.setAcceptedUser(friendshipRequest.getReceiver());
        friendship.setRequestedUser(friendshipRequest.getSender());

        friendshipRepository.save(friendship);

        return convertToDTO(friendshipRequest);
    }

    public void declineFriendRequest(Long requestId) {
        FriendshipRequest friendshipRequest = friendshipRequestRepository.findById(requestId).orElseThrow(() -> new IllegalStateException("Friend request not found"));
        friendshipRequest.setStatus(RequestStatus.DECLINED);
        friendshipRequestRepository.save(friendshipRequest);
    }

    public List<FriendshipRequestDTO> getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByAcceptedUserIdOrRequestedUserId(userId, userId);
        return friendships.stream()
                .map(this::convertToFriendshipDTO)
                .collect(Collectors.toList());
    }

    public void removeFriend(Long userId, Long friendId) {
        List<Friendship> friendships = friendshipRepository.findFriendshipBetweenUsers(userId, friendId);
        friendshipRepository.deleteAll(friendships);
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

    private FriendshipRequestDTO convertToFriendshipDTO(Friendship friendship) {
        FriendshipRequestDTO dto = new FriendshipRequestDTO();
        dto.setRequestId(friendship.getId());
        dto.setSenderId(friendship.getRequestedUser().getId());
        dto.setReceiverId(friendship.getAcceptedUser().getId());
        dto.setRequestTime(null);
        dto.setStatus(RequestStatus.ACCEPTED);
        return dto;
    }
}
