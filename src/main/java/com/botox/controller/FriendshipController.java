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
@RequestMapping("/api/friendship")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request")
    public ResponseEntity<String> sendFriendRequest(@RequestBody FriendshipRequestCreateRequest request) {
        try {
            FriendshipRequestDTO friendshipRequestDTO = friendshipService.sendFriendRequest(request);
            return new ResponseEntity<>("Friend request sent successfully", HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/requests/{userNickname}")
    public ResponseEntity<List<FriendshipRequestDTO>> getPendingFriendRequests(@PathVariable String userNickname) {
        List<FriendshipRequestDTO> pendingRequests = friendshipService.getPendingFriendRequests(userNickname);
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

    @GetMapping("/{userNickname}")
    public ResponseEntity<List<FriendshipRequestDTO>> getFriends(@PathVariable String userNickname) {
        List<FriendshipRequestDTO> friends = friendshipService.getFriends(userNickname);
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    @DeleteMapping("/remove/{userNickname}/{friendNickname}")
    public ResponseEntity<String> removeFriend(@PathVariable String userNickname, @PathVariable String friendNickname) {
        if (!friendshipService.isAlreadyFriends(userNickname, friendNickname)) {
            return new ResponseEntity<>("Not friends", HttpStatus.BAD_REQUEST);
        }

        friendshipService.removeFriend(userNickname, friendNickname);
        return new ResponseEntity<>("Friend removed successfully", HttpStatus.OK);
    }
}