package com.botox.domain;

import com.botox.constant.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
//기존에 코드에서 필요없는 getter와 setter 모두 없애고 lombok으로 대체
@Entity
@Table(name = "friendshiprequest")
@Getter
@Setter
@NoArgsConstructor
public class FriendshipRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "receiver_id")
    private Long receiverId;

    private LocalDateTime requestTime;

    @Enumerated(EnumType.STRING)
    private RequestStatus status; // enum: PENDING, ACCEPTED, DECLINED

    // 필요 없는 Getter와 Setter는 Lombok으로 대체
}


//package com.botox.domain;
//
//import com.botox.constant.RequestStatus;
//import jakarta.persistence.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "friendshiprequest")
//public class FriendshipRequest {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long requestId;
//
//    @ManyToOne
//    @JoinColumn(name = "sender_id")
//    private User sender;
//
//    @Column(name = "receiver_id")
//    private Long receiverId;
//
//    private LocalDateTime requestTime;
//
//    @Enumerated(EnumType.STRING)
//    private RequestStatus status; // enum: PENDING, ACCEPTED, DECLINED
//
//    // Getters, setters, constructors
//
//    // Getter and Setter methods
//    public Long getRequestId() {
//        return requestId;
//    }
//
//    public void setRequestId(Long requestId) {
//        this.requestId = requestId;
//    }
//
//    public User getSender() {
//        return sender;
//    }
//
//    public void setSender(User sender) {
//        this.sender = sender;
//    }
//
//    public Long getReceiverId() {
//        return receiverId;
//    }
//
//    public void setReceiverId(Long receiverId) {
//        this.receiverId = receiverId;
//    }
//
//    public LocalDateTime getRequestTime() {
//        return requestTime;
//    }
//
//    public void setRequestTime(LocalDateTime requestTime) {
//        this.requestTime = requestTime;
//    }
//
//    public RequestStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(RequestStatus status) {
//        this.status = status;
//    }
//}