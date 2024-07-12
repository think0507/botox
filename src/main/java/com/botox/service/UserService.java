package com.botox.service;

import com.botox.constant.UserStatus;
import com.botox.domain.*;
import com.botox.repository.UserRepository;
import com.botox.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public boolean existsByUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }

    public void validateDuplicateUser(User user) {
        if (existsByUserNickname(user.getUserNickname())) {
            throw new IllegalStateException("이미 존재하는 userNickname 입니다.");
        }
        if (existsByUserId(user.getUserId())) {
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
            String accessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(10));
            String refreshToken = tokenProvider.generateRefreshToken(user, Duration.ofDays(7));
            updateUserStatus(user.getUserId(), UserStatus.ONLINE);

            redisTemplate.opsForValue().set("TOKEN:" + user.getUserId(), accessToken, Duration.ofMinutes(10));
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
                String newAccessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(10));

                redisTemplate.opsForValue().set("TOKEN:" + userId, newAccessToken, Duration.ofMinutes(10
                    ));
                redisTemplate.opsForValue().set("REFRESH_TOKEN:" + userId, refreshToken, Duration.ofDays(7));

                return new LoginResponseDTO(user.getUserId(), user.getPassword(), newAccessToken, refreshToken, user.getStatus());
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


    public Optional<UserDTO> getUserByUserId(String userId) {
        return userRepository.findByUserId(userId).map(this::convertToDTO);
    }

    // 유저 삭제
    @Transactional
    public void deleteUser(String userId) {
        userRepository.deleteByUserId(userId);
    }
    // userProfile 조회
    public ProfileDTO getUserProfile(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return new ProfileDTO(userId, user.getUserNickname(), user.getUserProfile(), user.getUserProfilePic());
    }

    // userProfile 생성 또는 수정
    public ProfileDTO updateUserProfile(String userId, String userProfile, String userProfilePic, String userNickname) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserProfile(userProfile);
        user.setUserProfilePic(userProfilePic);
        user.setUserNickname(userNickname);
        User updatedUser = userRepository.save(user);
        return convertToProfileDTO(updatedUser);
    }

    // userProfile 삭제
    public ProfileDTO deleteUserProfile(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserProfile(null);
        user.setUserProfilePic(null);
        User updatedUser = userRepository.save(user);
        return convertToProfileDTO(updatedUser);
    }

    // User -> ProfileDTO 변환 메서드
    private ProfileDTO convertToProfileDTO(User user) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setUserId(user.getUserId());
        profileDTO.setUserNickname(user.getUserNickname());
        profileDTO.setUserProfile(user.getUserProfile());
        profileDTO.setUserProfilePic(user.getUserProfilePic());
        return profileDTO;
    }


    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUserId(user.getUserId());
        userDTO.setUserProfile(user.getUserProfile());
        userDTO.setUserProfilePic(user.getUserProfilePic());
        userDTO.setUserTemperatureLevel(user.getUserTemperatureLevel());
        userDTO.setUserNickname(user.getUserNickname());
        userDTO.setStatus(user.getStatus());
        return userDTO;
    }

}
