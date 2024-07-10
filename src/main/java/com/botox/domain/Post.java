package com.botox.domain;

import com.botox.constant.PostType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "post")
@Getter @Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime date;

    @Column(name = "likes_count")
    private Integer likesCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private PostType postType;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @Column(name = "comment_cnt")
    private Integer commentCnt;

    @PrePersist
    protected void onCreate() {
        date = LocalDateTime.now();
    }


    public Long getId() {
        return this.postId;
    }
}
