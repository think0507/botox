package com.botox.service;

import com.botox.domain.ProfileDTO;
import com.botox.domain.User;
import com.botox.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // 특정 유저 조회
    public Optional<User> getUserByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    // 유저 삭제
    @Transactional
    public void deleteUser(String userId) {
        userRepository.deleteByUserId(userId);
    }

    // userProfile 생성 또는 수정
    public User updateUserProfile(String userId, String userProfile, String userProfilePic) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserProfile(userProfile);
        user.setUserProfilePic(userProfilePic);
        return userRepository.save(user);
    }

    // userProfile 삭제
    public User deleteUserProfile(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserProfile(null);
        user.setUserProfilePic(null);
        return userRepository.save(user);
    }

    // userProfile 조회
    public ProfileDTO getUserProfile(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return new ProfileDTO(userId,user.getUserNickname(),user.getUserProfile(), user.getUserProfilePic());
    }

}
