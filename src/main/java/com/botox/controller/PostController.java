package com.botox.controller;

import com.botox.constant.MessageResponse;
import com.botox.constant.PostResponse;
import com.botox.domain.Post;
import com.botox.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        Post newPost = postService.createPost(post);
        return ResponseEntity.ok().body(new PostResponse(newPost.getId(), "Post created successfully"));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable Long postId) {
        Post post = postService.getPost(postId);
        return ResponseEntity.ok().body(post);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody Post postDetails) {
        postService.updatePost(postId, postDetails);
        return ResponseEntity.ok().body(new MessageResponse("Post updated successfully"));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok().body(new MessageResponse("Post deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if(size > 50) size = 50;
        Page<Post> posts = postService.getAllPosts(PageRequest.of(page, size, Sort.by("date").descending()));

        Map<String, Object> response = new HashMap<>();
        response.put("posts", posts.getContent());
        response.put("currentPage", posts.getNumber());
        response.put("totalItems", posts.getTotalElements());
        response.put("totalPages", posts.getTotalPages());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/mypage")
    public ResponseEntity<?> myPage() {
        Map<String, Object> response = new HashMap<>();
        response.put("redirect", "/mypage");
        response.put("message", "Redirecting to MyPage");
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/home")
    public ResponseEntity<?> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("redirect", "/home");
        response.put("message", "Redirecting to Home");
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/board")
    public ResponseEntity<?> board() {
        Map<String, Object> response = new HashMap<>();
        response.put("redirect", "/board");
        response.put("message", "Redirecting to Board");
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/friends")
    public ResponseEntity<?> friendsList() {
        Map<String, Object> response = new HashMap<>();
        response.put("redirect", "/friends");
        response.put("message", "Redirecting to Friends List");
        return ResponseEntity.ok().body(response);
    }
}