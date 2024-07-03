package com.botox.domain;

import com.botox.domain.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(columnDefinition = "TEXT")
    private String commentContent;  // Long에서 String으로 변경

    private int likesCount;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // Getters, setters, constructors
}
