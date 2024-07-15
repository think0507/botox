package com.botox.service;

import com.botox.controller.CommentController;
import com.botox.domain.Comment;
import com.botox.domain.Post;
import com.botox.domain.User;
import com.botox.exception.NotFoundCommentException;
import com.botox.repository.CommentRepository;
import com.botox.repository.PostRepository;
import com.botox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

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

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundCommentException("Comment not found"));

        commentRepository.delete(comment);
    }

    //댓글 좋아요 기능
    @Transactional
    public Comment likeComment(Long commentId){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(
                        ()-> new NotFoundCommentException("Comment not found")
                );

        comment.setLikesCount(comment.getLikesCount()+1);
        return commentRepository.save(comment);
    }
}
