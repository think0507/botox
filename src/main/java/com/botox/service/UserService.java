package com.botox.service;

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

    // 유저 생성
    public User createUser(User user) {
        return userRepository.save(user);
    }

//    // 모든 유저 조회
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }

    // 특정 유저 조회
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }


    // 유저 수정
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserProfile(userDetails.getUserProfile());
        user.setUserProfilePic(userDetails.getUserProfilePic());
        user.setUserNickname(userDetails.getUserNickname());
        user.setPassword(userDetails.getPassword());
        return userRepository.save(user);
    }

    // 유저 삭제
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // userProfile 생성 또는 수정
    public User updateUserProfile(Long id, String userProfile, String userProfilePic) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserProfile(userProfile);
        user.setUserProfilePic(userProfilePic);
        return userRepository.save(user);
    }

    // userProfile 삭제
    public User deleteUserProfile(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserProfile(null);
        user.setUserProfilePic(null);
        return userRepository.save(user);
    }

    // 비밀번호 변경
    public User updateUserPassword(Long id, String password) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(password);
        return userRepository.save(user);
    }
}
