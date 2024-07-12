package com.botox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "friendship")
@Getter @Setter
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "accept_user_id", referencedColumnName = "id")
    private User acceptUser;

    @ManyToOne
    @JoinColumn(name = "request_user_id", referencedColumnName = "id")
    private User requestUser;
}