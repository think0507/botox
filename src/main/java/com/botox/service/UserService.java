package com.botox.service;

import com.botox.config.jwt.TokenProvider;
import com.botox.constant.UserStatus;
import com.botox.domain.LoginResponseDTO;
import com.botox.domain.SpringUser;
import com.botox.domain.User;
import com.botox.domain.UserCreateForm;
import com.botox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserCreateForm createUser(UserCreateForm userCreateForm) {
        User newUser = new User();
        newUser.setUserId(userCreateForm.getUserId());
        newUser.setPassword(passwordEncoder.encode(userCreateForm.getPassword1()));
        newUser.setUserNickname(userCreateForm.getUserNickName());

        validateDuplicateUser(newUser);
        userRepository.save(newUser);
        return userCreateForm;
    }

    public boolean existsByUserNickname(String userNickname) {
        return userRepository.existsByUserNickname(userNickname);
    }

    public boolean existsByEmail(String userId) {
        return userRepository.existsByUserId(userId);
    }

    public void validateDuplicateUser(User user) {
        if (existsByUserNickname(user.getUserNickname())) {
            throw new IllegalStateException("이미 존재하는 userNickname 입니다.");
        }
        if (existsByEmail(user.getUserId())) {
            throw new IllegalStateException("이미 존재하는 userId 입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Optional<User> registeredUser = userRepository.findByUserId(userId);
        if (registeredUser.isEmpty()) {
            throw new UsernameNotFoundException(userId);
        }
        return SpringUser.getSpringUserDetails(registeredUser.get());
    }

    public Optional<User> findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    public LoginResponseDTO getAccessToken(User user, String rawPassword) {
        UserDetails userDetails;
        try {
            userDetails = loadUserByUsername(user.getUserId());
        } catch (Exception e) {
            return null;
        }

        if (passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            String accessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(1));
            String refreshToken = tokenProvider.generateRefreshToken(user, Duration.ofDays(7));
            updateUserStatus(user.getUserId(), UserStatus.ONLINE);

            redisTemplate.opsForValue().set("TOKEN:" + user.getUserId(), accessToken, Duration.ofMinutes(1));
            redisTemplate.opsForValue().set("REFRESH_TOKEN:" + user.getUserId(), refreshToken, Duration.ofDays(7));

            return new LoginResponseDTO(user.getUserId(), user.getPassword(), accessToken, refreshToken, UserStatus.ONLINE);
        }
        return null;
    }

    public void logout(String userId) {
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            updateUserStatus(user.getUserId(), UserStatus.OFFLINE);

            redisTemplate.delete("TOKEN:" + userId);
            redisTemplate.delete("REFRESH_TOKEN:" + userId);
        } else {
            throw new UsernameNotFoundException("User not found with userId: " + userId);
        }
    }

    public LoginResponseDTO refreshAccessToken(String userId, String refreshToken) {
        String storedRefreshToken = (String) redisTemplate.opsForValue().get("REFRESH_TOKEN:" + userId);
        if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String newAccessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(1));

                redisTemplate.opsForValue().set("TOKEN:" + userId, newAccessToken, Duration.ofMinutes(1));
                redisTemplate.opsForValue().set("REFRESH_TOKEN:" + userId, refreshToken, Duration.ofDays(7));

                return new LoginResponseDTO(user.getUserId(), user.getPassword(), newAccessToken, refreshToken,user.getStatus());
            }
        }
        return null;
    }

    public boolean validateAccessToken(String token) {
        return tokenProvider.validateToken(token);
    }

    private void updateUserStatus(String userId, UserStatus status) {
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setStatus(status);
            userRepository.save(user);
        }
    }
}

