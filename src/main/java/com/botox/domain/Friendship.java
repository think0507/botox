package com.botox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "friendship")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "accepted_user_nickname", referencedColumnName = "user_nickname")
    private User acceptedUser;

    @ManyToOne
    @JoinColumn(name = "requested_user_nickname", referencedColumnName = "user_nickname")
    private User requestedUser;
}