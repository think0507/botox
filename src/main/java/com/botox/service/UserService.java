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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> userRequestQueues = new ConcurrentHashMap<>();


    public void createUser(UserCreateForm userCreateForm) {
        User newUser = new User();
        newUser.setUsername(userCreateForm.getUsername());
        newUser.setPassword(passwordEncoder.encode(userCreateForm.getPassword1()));
        newUser.setUserNickname(userCreateForm.getUserNickName());

        validateDuplicateUser(newUser);
        userRepository.save(newUser);
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

    @Cacheable(value = "users", key = "#username")
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO);
    }

    @CachePut(value = "users", key = "#username")
    public UserDTO updateUserStatus(String username, UserStatus status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @CacheEvict(value = "users", key = "#username")
    @Transactional
    public void deleteUser(String username) {
        userRepository.deleteByUsername(username);
    }

    public ProfileDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
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
        String actorUsername = getActorUsernameFromRedis(token);
        if (actorUsername == null) {
            throw new UnauthorizedException("인증되지 않은 사용자입니다.");
        }

        String requestId = UUID.randomUUID().toString();
        String lockKey = "lock:user_temperature:" + targetUsername;
        RLock lock = redissonClient.getLock(lockKey);
        String requestKey = "request:user_temperature:" + targetUsername + ":" + requestId;

        // 사용자별 요청 큐에 현재 요청 추가
        userRequestQueues.computeIfAbsent(targetUsername, k -> new ConcurrentLinkedQueue<>()).add(requestId);

        try {
            if (lock.tryLock(2, 5, TimeUnit.SECONDS)) {
                try {
                    return performTemperatureUpdate(actorUsername, targetUsername, temperatureChange, requestKey);
                } finally {
                    lock.unlock();
                    // 요청 큐에서 현재 요청 제거
                    userRequestQueues.get(targetUsername).remove(requestId);
                }
            } else {
                return pollForResult(requestKey, targetUsername, requestId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("온도 변경 중 인터럽트가 발생했습니다.", e);
        }
    }
    // 실제 온도 업데이트 수행
    private UserDTO performTemperatureUpdate(String actorUsername, String targetUsername, int temperatureChange, String requestKey) {
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

        String redisKey = "user_temperature:" + actorUsername + ":" + targetUsername + ":" + LocalDate.now();
        Boolean canUpdate = redisTemplate.opsForValue().setIfAbsent(redisKey, "updated", Duration.ofDays(1));

        if (Boolean.FALSE.equals(canUpdate)) {
            throw new RuntimeException("이미 오늘 이 사용자의 온도를 변경했습니다.");
        }

        int newTemp = Math.max(0, Math.min(100, currentTemp + temperatureChange));
        target.setUserTemperatureLevel(newTemp);
        User updatedUser = userRepository.save(target);
        UserDTO result = convertToDTO(updatedUser);

        redisTemplate.opsForValue().set(requestKey, result, Duration.ofSeconds(5));

        return result;
    }

    // 결과 폴링
    private UserDTO pollForResult(String requestKey, String targetUsername, String requestId) throws InterruptedException {
        ConcurrentLinkedQueue<String> userQueue = userRequestQueues.get(targetUsername);
        // 최대 3초 동안 결과 대기

        for (int i = 0; i < 3; i++) {
            if (!userQueue.peek().equals(requestId)) {
                // 현재 요청이 큐의 맨 앞에 있지 않으면 대기
                Thread.sleep(1000);
                continue;
            }

            UserDTO result = (UserDTO) redisTemplate.opsForValue().get(requestKey);
            if (result != null) {
                userQueue.remove(requestId);
                return result;
            }
            Thread.sleep(1000);
        }
        userQueue.remove(requestId);
        throw new RuntimeException("온도 변경 요청 처리 시간이 초과되었습니다.");
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