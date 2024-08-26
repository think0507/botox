package com.botox.domain;

import com.botox.domain.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "popular_post")
@Getter
@Setter
public class PopularPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "selected_date")
    private LocalDate selectedDate;

    @Column(name = "likes_count")
    private Integer likesCount;
}