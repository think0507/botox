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
    @JoinColumn(name = "acceptId")
    private User acceptedUser;

    @ManyToOne
    @JoinColumn(name = "requestId")
    private User requestedUser;

    // Getters, setters, constructors
}

