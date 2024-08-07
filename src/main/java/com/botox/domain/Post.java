package com.botox.domain;

import com.botox.constant.PostType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime date;
    private int likesCount;

    @Enumerated(EnumType.STRING)
    private PostType postType; // enum: GENERAL, ANNOUNCEMENT, OTHER

    private int commentCnt;

    // Getters, setters, constructors
}
