package com.botox.service;

import com.botox.domain.PopularPost;
import com.botox.domain.Post;
import com.botox.repository.PopularPostRepository;
import com.botox.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class PopularPostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PopularPostRepository popularPostRepository;

    @Transactional
    @Scheduled(cron = "26 3 * * * ?")
    public void selectPopularPost() {
        log.info("Starting popular post selection at {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.withHour(3).withMinute(26).withSecond(0);
        LocalDateTime startOfPeriod = cutoffTime.minusDays(1);

        log.info("Searching for posts between {} and {}", startOfPeriod, cutoffTime);

        List<Post> mostLikedPosts = postRepository.findTopPostsByLikesCountForDate(startOfPeriod, cutoffTime);

        if (!mostLikedPosts.isEmpty()) {
            // 동률인 경우 랜덤으로 선택
            Post selectedPost = mostLikedPosts.get(new Random().nextInt(mostLikedPosts.size()));

            log.info("Selected popular post: id={}, title={}, likes={}",
                    selectedPost.getPostId(), selectedPost.getTitle(), selectedPost.getLikesCount());

            PopularPost popularPost = new PopularPost();
            popularPost.setPost(selectedPost);
            popularPost.setSelectedDate(LocalDate.now());
            popularPost.setLikesCount(selectedPost.getLikesCount());

            popularPostRepository.save(popularPost);
            log.info("Saved popular post for date: {}", LocalDate.now());
        } else {
            log.info("No posts found for the given period");
        }
    }

    public PopularPost getPopularPostForToday() {
        return popularPostRepository.findBySelectedDate(LocalDate.now())
                .orElse(null);
    }
}