package com.botox.controller;

import com.botox.constant.UserRole;
import com.botox.domain.Comment;
import com.botox.domain.Report;
import com.botox.constant.ReportType;
import com.botox.domain.User;
import com.botox.exception.NotFoundCommentException;
import com.botox.service.CommentService;
import com.botox.service.CommentReportService;
import com.botox.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class CommentController {
    private final CommentService commentService;
    private final CommentReportService commentReportService;
    private final UserService userService;

    @Autowired
    public CommentController(UserService userService, CommentService commentService, CommentReportService commentReportService){
        this.commentService=commentService;
        this.userService=userService;
        this.commentReportService = commentReportService;
    }

    //댓글 등록 메서드
    @PostMapping("/comments")
    public ResponseForm<CommentForm> createComment(@RequestBody CommentForm commentForm) {
        try {
            // CommentService를 사용하여 새로운 댓글을 생성합니다. commentForm 객체를 전달하여 createComment 메서드를 호출합니다.
            Comment comment = commentService.createComment(commentForm);

            // 생성된 Comment 객체를 CommentForm 객체로 변환합니다. 이 변환 작업은 보안 또는 데이터 전송을 위한 용도입니다.
            CommentForm createCommentForm = convertToCommentForm(comment);

            // 변환된 CommentForm 객체를 ResponseForm에 담아 클라이언트에게 HTTP 상태 코드와 함께 응답합니다.
            return new ResponseForm<>(HttpStatus.OK, createCommentForm, "댓글 등록을 성공적으로 완료했습니다!");
        } catch (Exception e) {
            // 예기치 않은 오류가 발생할 경우 로그를 기록하고, INTERNAL_SERVER_ERROR 상태 코드와 함께 오류 메시지를 반환합니다.
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Unexpected error occurred.");
        }
    }

    //작품 내 댓글 조회 기능
    @GetMapping("/posts/{postId}/comments")
    //여기서 List로 받는 이유는 여러 댓글을 출력하기 위함.
    public ResponseForm<List<CommentForm>> getCommentsByPostId(@PathVariable Long postId) {
        try {
            // CommentForm 객체를 담을 리스트를 생성합니다.
            List<CommentForm> commentForms = new ArrayList<>();
            // CommentService를 사용하여 postId에 해당하는 댓글들을 조회합니다.
            List<Comment> comments = commentService.getCommentsByPostId(postId);
            // 조회된 댓글들을 CommentForm 객체로 변환하여 리스트에 추가합니다.
            for (Comment comment : comments) {
                CommentForm commentForm = convertToCommentForm(comment);
                commentForms.add(commentForm);
            }

            // 변환된 CommentForm 리스트를 ResponseForm에 담아 클라이언트에게 HTTP 상태 코드와 함께 응답합니다.
            return new ResponseForm<>(HttpStatus.OK, commentForms, "OK");
        } catch (NotFoundCommentException e) {
            // 해당 댓글이 없는 경우 NO_CONTENT 상태 코드와 함께 메시지를 반환합니다.
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        } catch (Exception e) {
            // 예기치 않은 오류가 발생할 경우 로그를 기록하고, INTERNAL_SERVER_ERROR 상태 코드와 함께 오류 메시지를 반환합니다.
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Unexpected error occurred.");
        }
    }

    //댓글 삭제 기능
    @DeleteMapping("/comments/{commentId}")
    public ResponseForm<Void> deleteComment(@PathVariable Long commentId) {
        try {
            //현재 인증된 사용자를 가져옴
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            //현재 사용자를 데이터베이스에서 조회
            User currentUser = userService.findByUsername(currentUsername)
                    .orElseThrow(()->new UsernameNotFoundException("유저를 찾을 수 없습니다."));

            //댓글을 데이터베이스에서 조회
            Comment comment = commentService.findById(commentId)
                    .orElseThrow(()->new NotFoundCommentException("댓글을 찾을 수 없습니다."));

            //댓글 작성자와 현재 사용자가 일치하는지 확인
            if(!comment.getAuthor().getUsername().equals(currentUsername)) {
                //현재 사용자가 ADMIN 권한을 가지고 있는지 조회
                if (currentUser.getRole() != UserRole.ADMIN) {
                    return new ResponseForm<>(HttpStatus.FORBIDDEN, null, "댓글 삭제 권한이 없습니다.");
                }
            }


            // CommentService를 사용하여 댓글을 삭제합니다.
            commentService.deleteComment(commentId);
            // 삭제가 성공적으로 이루어진 경우 NO_CONTENT 상태 코드와 함께 메시지를 반환합니다.
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "댓글이 삭제되었습니다.");
        } catch (NotFoundCommentException e) {
            // 해당 댓글을 찾을 수 없는 경우 NOT_FOUND 상태 코드와 함께 메시지를 반환합니다.
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Exception e) {
            // 예기치 않은 오류가 발생할 경우 로그를 기록하고, INTERNAL_SERVER_ERROR 상태 코드와 함께 오류 메시지를 반환합니다.
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Unexpected error occurred.");
        }
    }

    //댓글 신고 기능 구현
    @PostMapping("/comments/{commentId}/report")
    public ResponseForm<ReportForm> reportComment(@PathVariable Long commentId, @RequestBody ReportForm reportForm) {
        try {
            // reportForm에 commentId를 설정합니다.
            reportForm.setReportedContentId(commentId);
            // CommentReportService를 사용하여 신고를 처리하고 결과를 ReportForm 객체로 반환받습니다.
            ReportForm reportedForm = commentReportService.reportComment(reportForm);

            // 처리된 ReportForm 객체를 ResponseForm에 담아 클라이언트에게 HTTP 상태 코드와 함께 응답합니다.
            return new ResponseForm<>(HttpStatus.OK, reportedForm, "댓글 신고가 성공적으로 접수되었습니다.");
        } catch (Exception e) {
            // 예기치 않은 오류가 발생할 경우 로그를 기록하고, INTERNAL_SERVER_ERROR 상태 코드와 함께 오류 메시지를 반환합니다.
            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Unexpected error occurred.");
        }
    }

    //댓글 좋아요 기능 구현
    @PostMapping("/comments/{commentId}/like")
    public ResponseForm<CommentForm> likeComment(@PathVariable Long commentId, @RequestBody Map<String, Long> requestBody) {
        Long userId = requestBody.get("userId");
        try {

            // CommentService를 사용하여 댓글에 좋아요를 추가합니다.
            Comment comment = commentService.likeComment(commentId,userId);
            // 좋아요가 추가된 댓글을 CommentForm 객체로 변환합니다.
            CommentForm likedCommentForm = convertToCommentForm(comment);
            // 변환된 CommentForm 객체를 ResponseForm에 담아 클라이언트에게 HTTP 상태 코드와 함께 응답합니다.
            return new ResponseForm<>(HttpStatus.OK, likedCommentForm, "댓글 좋아요를 성공적으로 완료했습니다.");
        } catch (NotFoundCommentException e) {
            // 해당 댓글을 찾을 수 없는 경우 NOT_FOUND 상태 코드와 함께 메시지를 반환합니다.
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Exception e) {
            // 예기치 않은 오류가 발생할 경우 로그를 기록하고, INTERNAL_SERVER_ERROR 상태 코드와 함께 오류 메시지를 반환합니다.

            log.error("Unexpected error", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Unexpected error occurred.");
        }
    }

    // Comment 객체를 CommentForm 객체로 변환합니다.

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
    public static class CommentForm {
        private Long userId; //새로 추가한 좋아요를 누른 유저의 ID
        private Long authorId; // 댓글 작성자
        private Long postId; //게시글의 ID
        private String commentContent; //댓글 내용
        private Integer likesCount; // 댓글의 좋아요 수
        private Long commentId; //댓글의 ID
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ReportForm {
        private Long reportingUserId;
        private String reportingUserNickname;
        private Long reportedUserId;
        private String reportedUserNickname;
        private Long reportedContentId;
        private String feedbackResult;
        private String reasonForReport;
        private ReportType reportType;
    }

    @Data
    @AllArgsConstructor
    public static class ResponseForm<T> {
        private HttpStatus status;
        private T data;
        private String message;
    }
}