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
    public Page<Post> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if(size > 50) size = 50;
        return postService.getAllPosts(PageRequest.of(page, size, Sort.by("date").descending()));
    }

    @GetMapping("/search")
    public Page<Post> searchPosts(@RequestParam String title, Pageable pageable) {
        return postService.searchPosts(title, pageable);
    }

    @GetMapping("/mypage")
    public ResponseEntity<?> myPage() {
        return ResponseEntity.ok().body(new MessageResponse("Redirecting to MyPage"));
    }

    @GetMapping("/home")
    public ResponseEntity<?> home() {
        return ResponseEntity.ok().body(new MessageResponse("Redirecting to Home"));
    }

    @GetMapping("/board")
    public ResponseEntity<?> board() {
        return ResponseEntity.ok().body(new MessageResponse("Redirecting to Board"));
    }

    @GetMapping("/friends")
    public ResponseEntity<?> friendsList() {
        return ResponseEntity.ok().body(new MessageResponse("Redirecting to Friends List"));
    }
}