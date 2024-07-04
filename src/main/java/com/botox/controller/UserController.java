package com.botox.controller;

import com.botox.domain.User;
import com.botox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    //유저 생성(회원가입)
    @PostMapping("/register")
    public ResponseForm<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return new ResponseForm<>(HttpStatus.OK, createdUser, "User created successfully");
    }

//    //모든 유저 조회
//    @GetMapping
//    public ResponseForm<List<User>> getAllUsers() {
//        List<User> users = userService.getAllUsers();
//        return new ResponseForm<>(HttpStatus.OK, users, "Users retrieved successfully");
//    }

    // 특정 유저 조회
    @GetMapping("/{id}")
    public ResponseForm<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return new ResponseForm<>(HttpStatus.OK, user, "User retrieved successfully");
    }

    // 유저 수정
    @PutMapping("/{id}")
    public ResponseForm<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        return new ResponseForm<>(HttpStatus.OK, updatedUser, "User updated successfully");
    }

    // 유저 삭제(회원탈퇴)
    @DeleteMapping("/{id}")
    public ResponseForm<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "User deleted successfully");
    }

    // userProfile 속성 수정 또는 생성
    @PatchMapping("/{id}/profile")
    public ResponseForm<User> updateUserProfile(@PathVariable Long id, @RequestBody String userProfile,@RequestBody String userProfilePic) {
        User updatedUser = userService.updateUserProfile(id, userProfile, userProfilePic);
        return new ResponseForm<>(HttpStatus.OK, updatedUser, "User profile updated successfully");
    }

    // userProfile 속성 삭제
    @DeleteMapping("/{id}/profile")
    public ResponseForm<User> deleteUserProfile(@PathVariable Long id) {
        User updatedUser = userService.deleteUserProfile(id);
        return new ResponseForm<>(HttpStatus.OK, updatedUser, "User profile deleted successfully");
    }

    // 비밀번호 변경
    @PatchMapping("/{id}/password")
    public ResponseForm<User> updateUserPassword(@PathVariable Long id, @RequestBody String password) {
        User updatedUser = userService.updateUserPassword(id, password);
        return new ResponseForm<>(HttpStatus.OK, updatedUser, "User password updated successfully");
    }

}
