package com.botox.service;

import com.botox.domain.ProfileDto;
import com.botox.domain.User;
import com.botox.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // 특정 유저 조회
    public Optional<User> getUserByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }


    // 유저 정보 수정
    @Transactional
    public User updateUser(String userId, User userDetails) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (userDetails.getUserNickname() != null) {
            user.setUserNickname(userDetails.getUserNickname());
        }
        if (userDetails.getPassword() != null) {
            user.setPassword(userDetails.getPassword());
        }

        return userRepository.save(user);
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
    public ProfileDto getUserProfile(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return new ProfileDto(user.getUserNickname(),user.getUserProfile(), user.getUserProfilePic());
    }

    // 비밀번호 변경
    public User updateUserPassword(String userId, String password) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(password);
        return userRepository.save(user);
    }

}
