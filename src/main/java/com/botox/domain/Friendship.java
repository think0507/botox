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

    @ManyToOne
    @JoinColumn(name = "requestId")
    private User requestedUser;

    // Getters, setters, constructors
}

