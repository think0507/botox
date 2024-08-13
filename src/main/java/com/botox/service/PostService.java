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
import org.springframework.web.multipart.MultipartFile;

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

    @Transactional
    public String uploadImage(MultipartFile file, Long userId, boolean isProfileImage) throws Exception {
        String imageUrl = s3UploadService.uploadFile(file);

        if (isProfileImage && userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
            user.setUserProfilePic(imageUrl);
            userRepository.save(user);
        }

        return imageUrl;
    }

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

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = getPost(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this post");
        }
        postRepository.delete(post);
    }

    public Page<Post> getAllPosts(int page, int size, Sort sort) {
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

    @Transactional
    public void likePost(Long postId) {
        Post post = getPost(postId);
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(Long postId) {
        Post post = getPost(postId);
        if (post.getLikesCount() > 0) {
            post.setLikesCount(post.getLikesCount() - 1);
            postRepository.save(post);
        } else {
            throw new RuntimeException("좋아요 수가 이미 0입니다.");
        }
    }

    public int getLikeCount(Long postId) {
        Post post = getPost(postId);
        return post.getLikesCount();
    }
}