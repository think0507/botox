package com.botox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "friendship")
@Getter
@Setter
@NoArgsConstructor
public class Friendship {
    // 하나의 엔티티는 단순히 ID로만 저장하고 필요할때 조인하는 방법으로 수정
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "acceptId")
    private User acceptedUser;

    @Column(name = "requestId")
    private Long requestedUserId;

    // Lombok will generate the getters, setters, and the no-args constructor
}