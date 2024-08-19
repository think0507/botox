package com.botox.controller;

import com.botox.constant.MessageResponse;
import com.botox.constant.PostResponse;
import com.botox.constant.ReportType;
import com.botox.domain.Post;
import com.botox.service.PostService;
import com.botox.service.ReportService;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@Slf4j
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseForm<PostForm> createPost(@RequestBody Post post, @RequestParam Long userId) {
        try {
            Post createdPost = postService.createPost(post, userId);
            PostForm postForm = convertToPostForm(createdPost);
            return new ResponseForm<>(HttpStatus.OK, postForm, "게시글 생성을 성공적으로 완료했습니다!");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Error creating post: " + e.getMessage());
        }
    }

    @GetMapping("/{postId}")
    public ResponseForm<PostForm> getPost(@PathVariable Long postId) {
        try {
            Post post = postService.getPost(postId);
            PostForm postForm = convertToPostForm(post);
            return new ResponseForm<>(HttpStatus.OK, postForm, "OK");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, "Post not found");
        }
    }

    @PutMapping("/{postId}")
    public ResponseForm<PostForm> updatePost(@PathVariable Long postId, @RequestBody Post postDetails) {
        try {
            Post updatedPost = postService.updatePost(postId, postDetails);
            PostForm postForm = convertToPostForm(updatedPost);
            return new ResponseForm<>(HttpStatus.OK, postForm, "게시글이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Error updating post: " + e.getMessage());
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseForm<Void> deletePost(@PathVariable Long postId) {
        try {
            postService.deletePost(postId);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "게시글이 삭제되었습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Error deleting post: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseForm<PagedPostResponse> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if(size > 50) size = 50;
        Page<Post> posts = postService.getAllPosts(page, size, Sort.by("date").descending());
        List<PostForm> postForms = posts.getContent().stream()
                .map(this::convertToPostForm)
                .collect(Collectors.toList());

        PagedPostResponse pagedResponse = new PagedPostResponse(
                postForms,
                posts.getNumber() + 1, // 0-based to 1-based
                posts.getSize(),
                posts.getTotalElements(),
                posts.getTotalPages()
        );

        return new ResponseForm<>(HttpStatus.OK, pagedResponse, "OK");
    }


    @GetMapping("/search")
    public ResponseForm<PagedPostResponse> searchPosts(
            @RequestParam String title,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if(size > 50) size = 50;
        Page<Post> posts = postService.searchPostsByTitleInAllPosts(title,
                PageRequest.of(page - 1, size, Sort.by("date").descending()));
        List<PostForm> postForms = posts.getContent().stream()
                .map(this::convertToPostForm)
                .collect(Collectors.toList());

        PagedPostResponse pagedResponse = new PagedPostResponse(
                postForms,
                posts.getNumber() + 1,
                posts.getSize(),
                posts.getTotalElements(),
                posts.getTotalPages()
        );

        return new ResponseForm<>(HttpStatus.OK, pagedResponse, "OK");
    }

    @PostMapping("/{postId}/report")
    public ResponseForm<ReportResponse.ReportData> reportPost(@PathVariable Long postId, @RequestBody ReportRequest reportRequest) {
        // 요청 수신 로그
        log.info("Received request to report post with ID: {} and data: {}", postId, reportRequest);

        try {
            // 게시글 조회
            Post post = postService.getPost(postId);

            // 게시글에 연결된 사용자가 없는 경우 로그
            if (post.getUser() == null) {
                String errorMessage = "Post with id " + postId + " has no associated user";
                log.warn(errorMessage);
                return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, errorMessage);
            }

            // 신고 요청 데이터 설정
            reportRequest.setReportedPostId(postId);
            reportRequest.setReportedUserId(post.getUser().getId());

            // 신고 처리
            ReportResponse response = reportService.reportPost(reportRequest);

            // 신고 성공 로그
            log.info("Successfully reported post with ID: {}. Report data: {}", postId, response.getData());

            // 성공 응답 반환
            return new ResponseForm<>(HttpStatus.OK, response.getData(), "게시글 신고가 성공적으로 접수되었습니다.");
        } catch (Exception e) {
            // 예외 발생 로그
            log.error("Unexpected error while reporting post with ID: {}. Data: {}", postId, reportRequest, e);

            // 오류 응답 반환
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "An error occurred: " + e.getMessage());
        }
    }


    private PostForm convertToPostForm(Post post) {
        return PostForm.builder()
                .postId(post.getId())
                .authorId(post.getUser().getId())
                .title(post.getTitle())
                .content(post.getContent())
                .likesCount(post.getLikesCount())
                .commentCnt(post.getCommentCnt())
                .postType(String.valueOf(post.getPostType()))
                .date(post.getDate())
                .build();
    }


    @Data
    @AllArgsConstructor
    public static class PagedPostResponse {
        private List<PostForm> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
    }
    @Data
    @Builder
    @AllArgsConstructor
    public static class PostForm {
        private Long postId;
        private Long authorId;
        private String title;
        private String content;
        private Integer likesCount;
        private Integer commentCnt;
        private String postType;
        private LocalDateTime date;
    }

    @Data
    public static class ReportRequest {
        private Long reportingUserId;
        private String reportingUserNickname;
        private Long reportedUserId;
        private String reportedUserNickname;
        private Long reportedPostId;
        private String feedbackResult;
        private String reasonForReport;
        private String reportType;
    }

    @Data
    public static class ReportResponse {
        private String status;
        private ReportData data;
        private String message;

        @Data
        public static class ReportData {
            private Long reportingUserId;
            private String reportingUserNickname;
            private Long reportedUserId;
            private String reportedUserNickname;
            private Long reportedPostId;
            private String feedbackResult;
            private String reasonForReport;
            private String reportType;
        }
    }

    @Data
    @AllArgsConstructor
    public static class ResponseForm<T> {
        private HttpStatus status;
        private T data;
        private String message;
    }
}