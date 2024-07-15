package com.botox.repository;

import com.botox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    void deleteByUsername(String username);

    // (1) 중복 유저 체크를 위한 기본 쿼리 (username, userNickName) 준비
    boolean existsByUserNickname(String userNickname);

    // 로그인 인증 시 유저 유무 확인 가능
    boolean existsByUsername(String username);
}
