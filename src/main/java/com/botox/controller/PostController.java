package com.botox.controller;

import com.botox.constant.PostType;
import com.botox.domain.PopularPost;
import com.botox.domain.Post;
import com.botox.logger.PostLogger;
import com.botox.service.PostService;
import com.botox.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import com.botox.service.*;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/api/posts")
@Slf4j
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private S3UploadService s3UploadService;
    @Autowired
    private UserService userService;


    @PostMapping("/upload")
    public ResponseForm<String> uploadImage(@RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "username", required = false) String username,
                                            @RequestParam(value = "isProfileImage", defaultValue = "false") boolean isProfileImage) {
        try {
            String imageUrl = s3UploadService.uploadFile(file);

            if (isProfileImage && username != null) {
                userService.updateProfileImage(username, file);
                return new ResponseForm<>(HttpStatus.OK, imageUrl, "프로필 이미지가 성공적으로 업로드되었습니다.");
            }

            return new ResponseForm<>(HttpStatus.OK, imageUrl, "이미지가 성공적으로 업로드되었습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Error uploading image: " + e.getMessage());
        }
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseForm<PostForm> createPost(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("userId") Long userId,
            @RequestParam("postType") String postType) {
        try {
            Post post = new Post();
            post.setTitle(title);
            post.setContent(content);
            post.setPostType(PostType.valueOf(postType));

            Post createdPost = postService.createPost(post, userId, file);
            PostForm postForm = convertToPostForm(createdPost);
            return new ResponseForm<>(HttpStatus.OK, postForm, "게시글 생성을 성공적으로 완료했습니다!");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Error creating post: " + e.getMessage());
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseForm<PostForm> createPostJson(@RequestBody PostCreateRequest request) {
        try {
            Post post = new Post();
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            post.setPostType(PostType.valueOf(request.getPostType()));

            Post createdPost = postService.createPost(post, request.getUserId(), null);
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
    public ResponseForm<PostForm> updatePost(@PathVariable Long postId, @RequestBody Post postDetails, @RequestParam Long userId) {
        log.info("Received update request: postId={}, userId={}, postDetails={}", postId, userId, postDetails);
        try {
            Post updatedPost = postService.updatePost(postId, postDetails, userId);
            PostForm postForm = convertToPostForm(updatedPost);
            return new ResponseForm<>(HttpStatus.OK, postForm, "게시글이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            log.error("Error updating post: ", e);
            return new ResponseForm<>(HttpStatus.FORBIDDEN, null, e.getMessage());
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseForm<Void> deletePost(@PathVariable Long postId, @RequestParam Long userId) {
        try {
            postService.deletePost(postId, userId);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "게시글이 삭제되었습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.FORBIDDEN, null, e.getMessage());
        }
    }

    @DeleteMapping("/admin/{postId}")
    public ResponseForm<Void> adminDeletePost(@PathVariable Long postId, @RequestParam Long userId) {
        if (!userService.isAdmin(userId)) {
            return new ResponseForm<>(HttpStatus.FORBIDDEN, null, "관리자만 이 기능을 사용할 수 있습니다.");
        }
        try {
            postService.deletePost(postId, userId);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "관리자 권한으로 게시글이 삭제되었습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Error deleting post: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseForm<PagedPostResponse> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if(size > 50) size = 50;
        Page<Post> posts = postService.getAllPostsWithPopular(page, size, Sort.by("date").descending());
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

    // 게시글 신고
    @PostMapping("/{postId}/report")
    public ResponseForm<ReportResponse.ReportData> reportPost(@PathVariable Long postId, @RequestBody ReportRequest reportRequest, HttpServletRequest request) {
        try {
            ReportResponse response = reportService.reportPost(reportRequest);
            return new ResponseForm<>(HttpStatus.OK, response.getData(), "게시글 신고가 성공적으로 접수되었습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/{postId}/like")
    public ResponseForm<Void> likePost(@PathVariable Long postId, @RequestParam Long userId) {
        try {
            postService.likePost(postId, userId);
            return new ResponseForm<>(HttpStatus.OK, null, "게시글에 좋아요를 눌렀습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, e.getMessage());
        }
    }

    @DeleteMapping("/{postId}/like")
    public ResponseForm<Void> unlikePost(@PathVariable Long postId, @RequestParam Long userId) {
        try {
            postService.unlikePost(postId, userId);
            return new ResponseForm<>(HttpStatus.OK, null, "게시글 좋아요를 취소했습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, e.getMessage());
        }
    }

    @GetMapping("/{postId}/likes")
    public ResponseForm<Integer> getLikes(@PathVariable Long postId) {
        try {
            int likeCount = postService.getLikeCount(postId);
            return new ResponseForm<>(HttpStatus.OK, likeCount, "게시글의 좋아요 수를 불러왔습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, e.getMessage());
        }
    }

    private PostForm convertToPostForm(Post post) {
        return PostForm.builder()
                .postId(post.getPostId())
                .authorNickname(post.getUser().getUserNickname())
                .authorProfilePic(post.getUser().getUserProfilePic())
                .title(post.getTitle())
                .content(post.getContent())
                .likesCount(post.getLikesCount())
                .commentCnt(post.getCommentCnt())
                .postType(String.valueOf(post.getPostType()))
                .date(post.getDate())
                .imageUrl(post.getImageUrl())
                .build();
    }



    @Data
    public static class PostCreateRequest {
        private String title;
        private String content;
        private Long userId;
        private String postType;
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
        private String authorNickname;
        private String authorProfilePic;
        private String title;
        private String content;
        private Integer likesCount;
        private Integer commentCnt;
        private String postType;
        private LocalDateTime date;
        private String imageUrl;
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
}