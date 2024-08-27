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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipRequestRepository friendshipRequestRepository;

    public FriendshipRequestDTO sendFriendRequest(FriendshipRequestCreateRequest request) {
        if (isAlreadyFriendsOrPending(request.getSenderNickname(), request.getReceiverNickname())) {
            throw new IllegalStateException("Friend request already sent or you are already friends");
        }

        User sender = userRepository.findByUserNickname(request.getSenderNickname()).orElseThrow(() -> new IllegalStateException("Sender not found"));
        User receiver = userRepository.findByUserNickname(request.getReceiverNickname()).orElseThrow(() -> new IllegalStateException("Receiver not found"));

        FriendshipRequest friendshipRequest = new FriendshipRequest();
        friendshipRequest.setSender(sender);
        friendshipRequest.setReceiver(receiver);
        friendshipRequest.setRequestTime(LocalDateTime.now());
        friendshipRequest.setStatus(RequestStatus.PENDING);

        friendshipRequestRepository.save(friendshipRequest);

        return convertToDTO(friendshipRequest);
    }

    public boolean isAlreadyFriendsOrPending(String senderNickname, String receiverNickname) {
        boolean alreadyFriends = isAlreadyFriends(senderNickname, receiverNickname);
        boolean pendingRequest = friendshipRequestRepository.findBySender_UserNicknameAndReceiver_UserNicknameAndStatus(senderNickname, receiverNickname, RequestStatus.PENDING).isPresent()
                || friendshipRequestRepository.findBySender_UserNicknameAndReceiver_UserNicknameAndStatus(receiverNickname, senderNickname, RequestStatus.PENDING).isPresent()
                || friendshipRequestRepository.findBySender_UserNicknameAndReceiver_UserNicknameAndStatus(senderNickname, receiverNickname, RequestStatus.ACCEPTED).isPresent()
                || friendshipRequestRepository.findBySender_UserNicknameAndReceiver_UserNicknameAndStatus(receiverNickname, senderNickname, RequestStatus.ACCEPTED).isPresent();

        return alreadyFriends || pendingRequest;
    }

    public boolean isAlreadyFriends(String senderNickname, String receiverNickname) {
        List<Friendship> friendships = friendshipRepository.findFriendshipBetweenUsers(senderNickname, receiverNickname);
        return !friendships.isEmpty();
    }

    public List<FriendshipRequestDTO> getPendingFriendRequests(String userNickname) {
        List<FriendshipRequest> sentRequests = friendshipRequestRepository.findBySender_UserNicknameAndStatus(userNickname, RequestStatus.PENDING);
        List<FriendshipRequest> receivedRequests = friendshipRequestRepository.findByReceiver_UserNicknameAndStatus(userNickname, RequestStatus.PENDING);

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

    public List<FriendshipRequestDTO> getFriends(String userNickname) {
        List<Friendship> friendships = friendshipRepository.findByAcceptedUser_UserNicknameOrRequestedUser_UserNickname(userNickname, userNickname);
        return friendships.stream()
                .map(this::convertToFriendshipDTO)
                .collect(Collectors.toList());
    }

    public void removeFriend(String userNickname, String friendNickname) {
        List<Friendship> friendships = friendshipRepository.findFriendshipBetweenUsers(userNickname, friendNickname);
        friendshipRepository.deleteAll(friendships);
    }

    private FriendshipRequestDTO convertToDTO(FriendshipRequest friendshipRequest) {
        FriendshipRequestDTO dto = new FriendshipRequestDTO();
        dto.setRequestId(friendshipRequest.getRequestId());
        dto.setSenderNickname(friendshipRequest.getSender().getUserNickname());
        dto.setReceiverNickname(friendshipRequest.getReceiver().getUserNickname());
        dto.setRequestTime(friendshipRequest.getRequestTime());
        dto.setStatus(friendshipRequest.getStatus());
        return dto;
    }

    private FriendshipRequestDTO convertToFriendshipDTO(Friendship friendship) {
        FriendshipRequestDTO dto = new FriendshipRequestDTO();
        dto.setRequestId(friendship.getId());
        dto.setSenderNickname(friendship.getRequestedUser().getUserNickname());
        dto.setReceiverNickname(friendship.getAcceptedUser().getUserNickname());
        dto.setRequestTime(null);
        dto.setStatus(RequestStatus.ACCEPTED);
        return dto;
    }
}