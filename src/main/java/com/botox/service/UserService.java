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
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final S3UploadService s3UploadService;

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
                    redisTemplate.opsForValue().set("TOKEN:" + user.getUsername(), accessToken, Duration.ofMinutes(10));
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

            redisTemplate.delete("TOKEN:" + username);
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

                redisTemplate.opsForValue().set("TOKEN:" + username, newAccessToken, Duration.ofMinutes(10));
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
    public ProfileDTO updateUserProfile(String username, String userProfile, String userNickname, String nickname) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserProfile(userProfile);
        user.setUserNickname(userNickname);
        User updatedUser = userRepository.save(user);
        return convertToProfileDTO(updatedUser);
    }

    @Transactional
    public ProfileDTO updateProfileImage(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            String imageUrl = s3UploadService.uploadFile(file);
            user.setUserProfilePic(imageUrl);
            User updatedUser = userRepository.save(user);
            return convertToProfileDTO(updatedUser);
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
}