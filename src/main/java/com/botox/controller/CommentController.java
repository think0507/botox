package com.botox.controller;

import com.botox.domain.Comment;
import com.botox.domain.Room;
import com.botox.exception.NotFoundCommentException;
import com.botox.service.CommentService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class CommentController {
    private final CommentService commentService;

    //댓글 작성 기능
    @PostMapping("/comments")
    public ResponseForm<CommentForm> createComment(@RequestBody CommentForm commentForm){
        try{
        Comment comment = commentService.createComment(commentForm);
        CommentForm createCommentForm = convertToCommentForm(comment);
        return new ResponseForm<>(HttpStatus.OK,createCommentForm,"댓글 등록을 성공적으로 완료했습니다!");
    } catch (Exception e){
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Unexpected error occurred.");
        }
    }

    //특정 게시물에 대한 댓글 조회
    @GetMapping("/posts/{postId}/comments")
    public ResponseForm<List<CommentForm>> getCommentsByPostId(@PathVariable Long postId) {
        try {
            List<CommentForm> commentForms = new ArrayList<>();
            List<Comment> comments = commentService.getCommentsByPostId(postId);

            for (Comment comment : comments) {
                CommentForm commentForm = convertToCommentForm(comment);
                commentForms.add(commentForm);
            }

            // 상태 코드, 데이터, 메시지를 포함한 응답 반환
            return new ResponseForm<>(HttpStatus.OK, commentForms, "OK");
        } catch (NotFoundCommentException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Unexpected error occurred.");
        }
    }

    //댓글 삭제 기능
    @DeleteMapping("/comments/{commentId}")
    public ResponseForm<Void> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "댓글이 삭제되었습니다.");
        } catch (NotFoundCommentException e) {
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Unexpected error occurred.");
        }
    }



    private CommentForm convertToCommentForm(Comment comment) {
        return CommentForm.builder()
                .commentId(comment.getCommentId())
                .authorId(comment.getAuthor().getId())
                .postId(comment.getPost().getPostId())
                .likesCount(comment.getLikesCount())
                .commentContent(comment.getCommentContent())
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class CommentForm{
        private Long authorId;
        private Long postId;
        private String commentContent;
        private Integer likesCount;
        private Long commentId;

    }

    @Data
    @AllArgsConstructor
    public static class ResponseForm<T> {
        private HttpStatus status;
        private T data;
        private String message;
    }



}
