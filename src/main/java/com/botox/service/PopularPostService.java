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
        log.debug("Starting popular post selection at {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.withHour(3).withMinute(26).withSecond(0);
        LocalDateTime startOfPeriod = cutoffTime.minusDays(1);

        Post mostLikedPost = postRepository.findTopByDateBetweenOrderByLikesCountDesc(startOfPeriod, cutoffTime)
                .orElse(null);

        if (mostLikedPost != null) {
            PopularPost popularPost = new PopularPost();
            popularPost.setPost(mostLikedPost);
            popularPost.setSelectedDate(LocalDate.now());
            popularPost.setLikesCount(mostLikedPost.getLikesCount());

            popularPostRepository.save(popularPost);
            log.debug("Finished popular post selection");
        }
    }

    public PopularPost getPopularPostForToday() {
        return popularPostRepository.findBySelectedDate(LocalDate.now())
                .orElse(null);
    }
}