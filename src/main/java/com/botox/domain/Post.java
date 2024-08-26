package com.botox.domain;

import com.botox.constant.PostType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "post")
@Getter @Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime date;

    @Column(name = "likes_count")
    private Integer likesCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private PostType postType;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @Column(name = "comment_cnt")
    private Integer commentCnt = 0;

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "post_liked_users", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user_id")
    private Set<Long> likedUserIds = new HashSet<>();

    @OneToOne(mappedBy = "post")
    private PopularPost popularPost;

    @PrePersist
    protected void onCreate() {
        date = LocalDateTime.now();
    }
}