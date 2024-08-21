package com.botox.repository;

import com.botox.domain.Comment;
import com.botox.domain.CommentLike;
import com.botox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike,Long> {
    // 특정 사용자와 댓글 간의 좋아요 관계를 조회하는 메서드
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

}
