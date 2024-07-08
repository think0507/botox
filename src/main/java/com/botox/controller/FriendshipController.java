package com.botox.controller;

import com.botox.domain.FriendshipRequestCreateRequest;
import com.botox.domain.FriendshipRequestDTO;
import com.botox.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friendship")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request")
    public ResponseEntity<String> sendFriendRequest(@RequestBody FriendshipRequestCreateRequest request) {
        if (friendshipService.isAlreadyFriendsOrPending(request.getSenderId(), request.getReceiverId())) {
            return new ResponseEntity<>("Friend request already sent or you are already friends", HttpStatus.BAD_REQUEST);
        }

        FriendshipRequestDTO friendshipRequestDTO = friendshipService.sendFriendRequest(request);
        return new ResponseEntity<>("Friend request sent successfully", HttpStatus.CREATED);
    }

    @GetMapping("/requests/{userId}")
    public ResponseEntity<List<FriendshipRequestDTO>> getPendingFriendRequests(@PathVariable Long userId) {
        List<FriendshipRequestDTO> pendingRequests = friendshipService.getPendingFriendRequests(userId);
        return new ResponseEntity<>(pendingRequests, HttpStatus.OK);
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<FriendshipRequestDTO> acceptFriendRequest(@PathVariable Long requestId) {
        FriendshipRequestDTO friendshipRequestDTO = friendshipService.acceptFriendRequest(requestId);
        return new ResponseEntity<>(friendshipRequestDTO, HttpStatus.OK);
    }

    @PostMapping("/requests/{requestId}/decline")
    public ResponseEntity<Void> declineFriendRequest(@PathVariable Long requestId) {
        friendshipService.declineFriendRequest(requestId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<List<FriendshipRequestDTO>> getFriends(@PathVariable Long userId) {
        List<FriendshipRequestDTO> friends = friendshipService.getFriends(userId);
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }
}