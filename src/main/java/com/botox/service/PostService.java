package com.botox.service;

import com.botox.domain.Post;
import com.botox.domain.User;
import com.botox.repository.PostRepository;
import com.botox.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Post createPost(Post post, Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
            post.setUser(user);
            return postRepository.save(post);
        } catch (Exception e) {
            log.error("Error creating post: ", e);
            throw new RuntimeException("Failed to create post", e);
        }
    }

    public Post getPost(Long postId) {
        return postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id " + postId));
    }

    public Post updatePost(Long postId, Post postDetails) {
        Post post = getPost(postId);
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        post.setPostType(postDetails.getPostType());
        return postRepository.save(post);
    }

    public void deletePost(Long postId) {
        Post post = getPost(postId);
        postRepository.delete(post);
    }

    public Page<Post> getAllPosts(int page, int size, Sort sort) {
        // 1부터 시작하는 페이지 번호를 0부터 시작하는 번호로 변환
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return postRepository.findAll(pageable);
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
    }
