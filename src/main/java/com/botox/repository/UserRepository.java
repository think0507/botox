package com.botox.repository;

import com.botox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    void deleteByUserId(String userId);

    // (1) 중복 유저 체크를 위한 기본 쿼리 (userId, userNickName) 준비
    boolean existsByUserNickname(String userNickname);

    // 로그인 인증 시 유저 유무 확인 가능
    boolean existsByUserId(String userId);
}
