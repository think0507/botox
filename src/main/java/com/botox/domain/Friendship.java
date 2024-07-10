package com.botox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "friendship")
@Getter @Setter
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "acceptId", referencedColumnName = "user_id")
    private User acceptUser;

    @ManyToOne
    @JoinColumn(name = "requestId", referencedColumnName = "user_id")
    private User requestUser;
}