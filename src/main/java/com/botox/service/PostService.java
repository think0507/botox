package com.botox.service;

import com.botox.domain.PopularPost;
import com.botox.domain.Post;
import com.botox.domain.User;
import com.botox.repository.PostRepository;
import com.botox.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private S3UploadService s3UploadService;
    @Autowired
    private UserService userService;
    @Autowired
    private PopularPostService popularPostService;


    @Transactional
    public Post createPost(Post post, Long userId, MultipartFile file) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
            post.setUser(user);

            if (file != null && !file.isEmpty()) {
                String imageUrl = s3UploadService.uploadFile(file);
                post.setImageUrl(imageUrl);
            }

            return postRepository.save(post);
        } catch (Exception e) {
            log.error("Error creating post: ", e);
            throw new RuntimeException("Failed to create post", e);
        }
    }

    public Page<Post> getAllPostsWithPopular(int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Post> posts = postRepository.findAll(pageable);

        if (page == 1) {
            PopularPost popularPost = popularPostService.getPopularPostForToday();
            if (popularPost != null) {
                List<Post> content = new ArrayList<>();
                content.add(popularPost.getPost());
                content.addAll(posts.getContent());
                return new PageImpl<>(content, pageable, posts.getTotalElements() + 1);
            }
        }

        return posts;
    }


    public Post getPost(Long postId) {
        return postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id " + postId));
    }

    @Transactional
    public Post updatePost(Long postId, Post postDetails, Long userId) {
        log.info("Updating post: postId={}, userId={}", postId, userId);
        Post post = getPost(postId);
        log.info("Found post: {}", post);
        if (!post.getUser().getId().equals(userId)) {
            log.warn("User {} is not authorized to update post {}", userId, postId);
            throw new RuntimeException("You are not authorized to update this post");
        }

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to update this post");
        }
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        post.setPostType(postDetails.getPostType());
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = getPost(postId);
        if (!userService.canDeletePost(userId, post)) {
            throw new RuntimeException("You are not authorized to delete this post");
        }
        postRepository.delete(post);
    }

    public Page<Post> searchPostsByTitleInAllPosts(String title, Pageable pageable) {
        List<Post> allPosts = postRepository.findAll();
        List<Post> filteredPosts = allPosts.stream()
                .filter(post -> post.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredPosts.size());
        List<Post> pageContent = filteredPosts.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredPosts.size());
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = getPost(postId);
        if (post.getLikedUserIds().contains(userId)) {
            throw new RuntimeException("이미 이 게시글에 좋아요를 눌렀습니다.");
        }
        post.getLikedUserIds().add(userId);
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        Post post = getPost(postId);
        if (!post.getLikedUserIds().contains(userId)) {
            throw new RuntimeException("좋아요를 누르지 않은 게시글입니다.");
        }
        post.getLikedUserIds().remove(userId);
        post.setLikesCount(post.getLikesCount() - 1);
        postRepository.save(post);
    }
    public int getLikeCount(Long postId) {
        Post post = getPost(postId);
        return post.getLikesCount();
    }
}