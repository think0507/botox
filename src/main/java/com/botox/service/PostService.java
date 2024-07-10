package com.botox.service;

import com.botox.domain.Post;
import com.botox.exception.ResourceNotFoundException;
import com.botox.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id " + postId));
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

    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Page<Post> searchPosts(String title, Pageable pageable) {
        return postRepository.findByTitleContaining(title, pageable);
    }
}
