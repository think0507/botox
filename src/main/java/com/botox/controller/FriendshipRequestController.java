package com.botox.controller;

import com.botox.domain.FriendshipRequest;
import com.botox.domain.FriendshipRequestCreateRequest;
import com.botox.domain.FriendshipRequestDTO;
import com.botox.service.FriendshipRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friendshipRequest")
public class FriendshipRequestController {

    private final FriendshipRequestService friendshipRequestService;

    // 친구 요청 보내기
    @PostMapping("/send")
    public ResponseForm<FriendshipRequestDTO> sendFriendRequest(@RequestBody FriendshipRequestCreateRequest request) {
        FriendshipRequestDTO friendshipRequest = friendshipRequestService.sendFriendRequest(request.getSenderId(), request.getReceiverId());
        return new ResponseForm<>(HttpStatus.OK, friendshipRequest, "Friendship request sent successfully");
    }

//    // 친구 요청 수락 또는 거절
//    @PostMapping("/respond")
//    public ResponseForm<FriendshipRequest> respondToFriendRequest(@RequestParam Long requestId, @RequestParam String status) {
//        FriendshipRequest friendshipRequest = friendshipRequestService.respondToFriendRequest(requestId, status);
//        return new ResponseForm<>(HttpStatus.OK, friendshipRequest, "Friendship request responded successfully");
//    }

}
