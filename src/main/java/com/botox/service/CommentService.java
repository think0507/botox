package com.botox.service;

import com.botox.controller.CommentController;
import com.botox.domain.Comment;
import com.botox.domain.CommentLike;
import com.botox.domain.Post;
import com.botox.domain.User;
import com.botox.exception.NotFoundCommentException;
import com.botox.repository.CommentLikeRepository;
import com.botox.repository.CommentRepository;
import com.botox.repository.PostRepository;
import com.botox.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;
    //댓글 생성
    @Transactional
    public Comment createComment(CommentController.CommentForm commentForm) {
        User author = userRepository.findById(commentForm.getAuthorId())
                .orElseThrow(
                        () -> new RuntimeException("User가 존재하지 않습니다.")
                );

        Post post = postRepository.findById(commentForm.getPostId())
                .orElseThrow(
                        () -> new RuntimeException("Post가 존재하지 않습니다.")
                );

        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setCommentContent(commentForm.getCommentContent());
        comment.setLikesCount(0);

        return commentRepository.save(comment);

    }


    //댓글 조회
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPost_PostId(postId);
    }

   //댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundCommentException("Comment not found"));

        commentRepository.delete(comment);
    }

    //댓글 좋아요 기능
    @Transactional
    public Comment likeComment(Long commentId, Long userId){
        //commentID를 DB와 확인해 실존하는 ID인지 확인하고 틀리면 에러 출력
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(
                        ()-> new NotFoundCommentException("Comment not found")
                );
        //userID를 DB와 확인해 실존하는 ID인지 확인하고 틀리면 에러 출력
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        //사용자가 해당 댓글에 좋아요를 이미 눌렀는지 확인
        Optional<CommentLike> existingLike = commentLikeRepository.findByUserAndComment(user, comment);

        //만약 좋아요가 이미 눌려있었다면
        if (existingLike.isPresent()) {
            // 좋아요 취소
            comment.getLikes().remove(existingLike.get());
            comment.setLikesCount(comment.getLikesCount() - 1);
            commentLikeRepository.delete(existingLike.get());
        } else {
            // 좋아요 추가
            CommentLike like = new CommentLike();
            like.setUser(user);
            like.setComment(comment);

            comment.getLikes().add(like);
            comment.setLikesCount(comment.getLikesCount() + 1);
            commentLikeRepository.save(like);
        }
        return commentRepository.save(comment);
    }

    // 댓글 ID로 조회 (새로 추가된 메서드)
    @Transactional(readOnly = true)
    public Optional<Comment> findById(Long commentId) {
        return commentRepository.findById(commentId);
    }
}
