package com.botox.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(columnDefinition = "TEXT")
    private String commentContent;
    private int likesCount;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // Getters, setters, constructors
}
