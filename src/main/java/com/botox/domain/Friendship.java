package com.botox.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "friendship")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "acceptId")
    private User acceptedUser;

    @Column(name = "requestId")
    private Long requestedUserId;

    // Getters, setters, constructors
}

