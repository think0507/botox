package com.botox.repository;

import com.botox.domain.PopularPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PopularPostRepository extends JpaRepository<PopularPost, Long> {

    Optional<PopularPost> findBySelectedDate(LocalDate date);
}