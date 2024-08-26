package com.botox.service;

import com.botox.constant.UserRole;
import com.botox.constant.UserStatus;
import com.botox.domain.*;
import com.botox.exception.UnauthorizedException;
import com.botox.repository.UserRepository;
import com.botox.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final S3UploadService s3UploadService;
    private final RedissonClient redissonClient;

    public UserCreateForm createUser(UserCreateForm userCreateForm) {
        User newUser = new User();
        newUser.setUsername(userCreateForm.getUsername());
        newUser.setPassword(passwordEncoder.encode(userCreateForm.getPassword1()));
        newUser.setUserNickname(userCreateForm.getUserNickName());

        validateDuplicateUser(newUser);
        userRepository.save(newUser);
        return userCreateForm;
    }

    public boolean existsByUserNickname(String userNickname) {
        return userRepository.existsByUserNickname(userNickname);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void validateDuplicateUser(User user) {
        if (existsByUserNickname(user.getUserNickname())) {
            throw new IllegalStateException("이미 존재하는 userNickname 입니다.");
        }
        if (existsByUsername(user.getUsername())) {
            throw new IllegalStateException("이미 존재하는 username 입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> registeredUser = userRepository.findByUsername(username);
        if (registeredUser.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        return SpringUser.getSpringUserDetails(registeredUser.get());
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public LoginResponseDTO getAccessToken(User user, String rawPassword) {
        UserDetails userDetails;
        try {
            userDetails = loadUserByUsername(user.getUsername());
        } catch (Exception e) {
            System.err.println("Error loading user by username: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        if (passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            try {
                String accessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(10));
                String refreshToken = tokenProvider.generateRefreshToken(user, Duration.ofDays(7));
                updateUserStatus(user.getUsername(), UserStatus.ONLINE);

                try {
                    redisTemplate.opsForValue().set("ACCESS_TOKEN:" + user.getUsername(), accessToken, Duration.ofMinutes(10));
                    redisTemplate.opsForValue().set("REFRESH_TOKEN:" + user.getUsername(), refreshToken, Duration.ofDays(7));
                } catch (Exception e) {
                    System.err.println("Error storing tokens in Redis: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }

                return new LoginResponseDTO(user.getUsername(), user.getPassword(), accessToken, refreshToken, UserStatus.ONLINE);
            } catch (Exception e) {
                System.err.println("Error generating tokens: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } else {
            System.err.println("Password mismatch for user: " + user.getUsername());
        }
        return null;
    }

    public void logout(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            updateUserStatus(user.getUsername(), UserStatus.OFFLINE);

            redisTemplate.delete("ACCESS_TOKEN:" + username);
            redisTemplate.delete("REFRESH_TOKEN:" + username);
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    public LoginResponseDTO refreshAccessToken(String username, String refreshToken) {
        String storedRefreshToken = (String) redisTemplate.opsForValue().get("REFRESH_TOKEN:" + username);
        if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String newAccessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(10));

                redisTemplate.opsForValue().set("ACCESS_TOKEN:" + username, newAccessToken, Duration.ofMinutes(10));
                redisTemplate.opsForValue().set("REFRESH_TOKEN:" + username, refreshToken, Duration.ofDays(7));

                return new LoginResponseDTO(user.getUsername(), user.getPassword(), newAccessToken, refreshToken, user.getStatus());
            }
        }
        return null;
    }

    public boolean validateAccessToken(String token) {
        return tokenProvider.validateToken(token);
    }

    private void updateUserStatus(String username, UserStatus status) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setStatus(status);
            userRepository.save(user);
        }
    }

    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(this::convertToDTO);
    }

    @Transactional
    public void deleteUser(String username) {
        userRepository.deleteByUsername(username);
    }

    public ProfileDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return new ProfileDTO(username, user.getUserNickname(), user.getUserProfile(), user.getUserProfilePic());
    }

    @Transactional
    public UserDTO updateUserProfile(String username, String userProfile, String userNickname, MultipartFile file) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userProfile != null) {
            user.setUserProfile(userProfile);
        }
        if (userNickname != null) {
            user.setUserNickname(userNickname);
        }
        if (file != null && !file.isEmpty()) {
            String imageUrl = s3UploadService.uploadFile(file);
            user.setUserProfilePic(imageUrl);
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Transactional
    public UserDTO updateProfileImage(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            String imageUrl = s3UploadService.uploadFile(file);
            user.setUserProfilePic(imageUrl);
            User updatedUser = userRepository.save(user);
            return convertToDTO(updatedUser);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update profile image", e);
        }
    }

    public ProfileDTO deleteUserProfile(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserProfile(null);
        user.setUserProfilePic(null);
        User updatedUser = userRepository.save(user);
        return convertToProfileDTO(updatedUser);
    }

    private ProfileDTO convertToProfileDTO(User user) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setUsername(user.getUsername());
        profileDTO.setUserNickname(user.getUserNickname());
        profileDTO.setUserProfile(user.getUserProfile());
        profileDTO.setUserProfilePic(user.getUserProfilePic());
        return profileDTO;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setUserProfile(user.getUserProfile());
        userDTO.setUserProfilePic(user.getUserProfilePic());
        userDTO.setUserTemperatureLevel(user.getUserTemperatureLevel());
        userDTO.setUserNickname(user.getUserNickname());
        userDTO.setStatus(user.getStatus());
        return userDTO;
    }

    // 유저 온도 증가
    @Transactional
    public UserDTO increaseUserTemperature(String token, String targetUsername) {
        return updateUserTemperature(token, targetUsername, 1);
    }

    // 유저 온도 감소
    @Transactional
    public UserDTO decreaseUserTemperature(String token, String targetUsername) {
        return updateUserTemperature(token, targetUsername, -1);
    }

    // 유저 온도 변경
    @Transactional
    public UserDTO updateUserTemperature(String token, String targetUsername, int temperatureChange) {
        String lockKey = "lock:user_temperature:" + targetUsername;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도 (최대 2초 대기, 5초 후 자동 해제)
            if (lock.tryLock(2, 5, TimeUnit.SECONDS)) {
                try {
                    String actorUsername = getActorUsernameFromRedis(token);
                    if (actorUsername == null) {
                        throw new UnauthorizedException("인증되지 않은 사용자입니다.");
                    }

                    if (actorUsername.equals(targetUsername)) {
                        throw new IllegalArgumentException("자신의 온도는 변경할 수 없습니다.");
                    }

                    User target = userRepository.findByUsername(targetUsername)
                            .orElseThrow(() -> new RuntimeException("Target user not found"));

                    int currentTemp = target.getUserTemperatureLevel() != null ? target.getUserTemperatureLevel() : 0;

                    if ((currentTemp == 0 && temperatureChange < 0) || (currentTemp == 100 && temperatureChange > 0)) {
                        throw new IllegalStateException("온도를 더 이상 " +
                                (temperatureChange > 0 ? "올릴" : "내릴") + " 수 없습니다.");
                    }

                    // 날짜 별 온도 변경 redis 에 저장
                    String redisKey = "user_temperature:" + actorUsername + ":" + targetUsername + ":" + LocalDate.now();
                    Boolean canUpdate = redisTemplate.opsForValue().setIfAbsent(redisKey, "updated", Duration.ofDays(1));

                    if (Boolean.FALSE.equals(canUpdate)) {
                        throw new RuntimeException("이미 오늘 이 사용자의 온도를 변경했습니다.");
                    }

                    int newTemp = currentTemp + temperatureChange;
                    newTemp = Math.max(0, Math.min(100, newTemp));

                    target.setUserTemperatureLevel(newTemp);
                    User updatedUser = userRepository.save(target);

                    return convertToDTO(updatedUser);
                } finally {
                    lock.unlock();
                }
            } else {
                throw new RuntimeException("온도 변경을 위한 락 획득에 실패했습니다.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("온도 변경 중 인터럽트가 발생했습니다.", e);
        }
    }

    // Redis 에서 토큰을 통해 사용자 이름 획득
    public String getActorUsernameFromRedis(String token) {
        Set<String> keys = redisTemplate.keys("ACCESS_TOKEN:*");
        if (keys != null) {
            for (String key : keys) {
                Object storedToken = redisTemplate.opsForValue().get(key);
                if (storedToken != null && token.equals(storedToken.toString())) {
                    return key.replace("ACCESS_TOKEN:", "");
                }
            }
        }
        return null;
    }

    public boolean isAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRole() == UserRole.ADMIN;
    }

    public boolean canDeletePost(Long userId, Post post) {
        if (isAdmin(userId)) {
            return true;  // 관리자는 모든 게시글 삭제 가능
        }
        return post.getUser().getId().equals(userId);  // 자신의 게시글만 삭제 가능
    }

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
}