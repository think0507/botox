package com.botox.service;

import com.botox.config.jwt.TokenProvider;
import com.botox.domain.AccessTokenDTO;
import com.botox.domain.SpringUser;
import com.botox.domain.User;
import com.botox.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public void createUser(
            String userNickname,
            String password1,
            String userId
    ) {
        User newUser = new User();
        newUser.setUserNickname(userNickname);
        newUser.setPassword(
                passwordEncoder.encode(password1)
        );
        newUser.setUserId(userId);

        // 중복 유저 체크
        validateDuplicateUser(newUser);
        User savedUser = userRepository.save(newUser);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUserNickname(username);
    }

    public boolean existsByEmail(String username) {
        return userRepository.existsByUserId(username);
    }

    public void validateDuplicateUser(User user) {
        // username 중복 검사
        if (existsByUsername(user.getUserNickname())) {
            throw new IllegalStateException("이미 존재하는 username 입니다.");
        }
        // email 중복 검사
        if (existsByEmail(user.getUserId())) {
            throw new IllegalStateException("이미 존재하는 email 입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(
            String userId  // 로그인 ID 를 말함
    ) throws UsernameNotFoundException {
        Optional<User> registeredUser = userRepository.findByUserId(userId);
        if (registeredUser.isEmpty()) {
            throw new UsernameNotFoundException(userId);
        }
//        User foundUser = registeredUser.get();
//        return new SpringUser( // 인증에 사용하기 위해 준비된 UserDetails 구현체
//            foundUser.getEmail(),
//            foundUser.getPassword(),
//            new ArrayList<>()
//        );
        return SpringUser.getSpringUserDetails(registeredUser.get());
    }

    public AccessTokenDTO getAccessToken(User user) {
        UserDetails userDetails;
        // 1) Spring Security 로그인 전용 메서드 loadUserByUsername 사용해 인증
        try {
            userDetails = loadUserByUsername(user.getUserId());
        } catch (Exception e) {
            return null;
        }
        // 2) UserService 에 TokenProvider 주입 -> Done
        // 3) TokenProvider 에서 Token String 을 생성
        //    - 비밀번호 체크
        if (passwordEncoder.matches(user.getPassword(), userDetails.getPassword())) {
            // 4) AccessTokenDTO 로 Wrapping 및 리턴
            String accessToken = tokenProvider.generateToken(user, Duration.ofHours(1L));
            String tokenType = "Bearer";
            return new AccessTokenDTO(
                    accessToken, tokenType
            );
        }
        return null;
    }
}
