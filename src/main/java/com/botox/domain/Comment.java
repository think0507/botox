package com.botox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "comment")
@Getter @Setter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "user_id")
    private User author;

    @Column(name = "comment_content", columnDefinition = "TEXT")
    private String commentContent;

    @Column(name = "likes_count")
    private Integer likesCount;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}