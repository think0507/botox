package com.botox.controller;

import com.botox.config.jwt.TokenProvider;
import com.botox.constant.UserStatus;
import com.botox.domain.ProfileDTO;
import com.botox.domain.User;
import com.botox.domain.*;
import com.botox.exception.UnauthorizedException;
import com.botox.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseForm<UserCreateForm> createUser(
            @Validated @RequestBody UserCreateForm userCreateForm,
            BindingResult bindingResult
    ) {
        // 1. Form 데이터 검증
        if (bindingResult.hasErrors()) {
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, "입력값에 오류가 있습니다.");
        }
        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordDoubleCheckError", "패스워드 확인 값이 일치하지 않습니다.");
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, "패스워드 확인 값이 일치하지 않습니다.");
        }

        // 2. 백엔드 validation
        try {
            userService.createUser(userCreateForm);
        } catch (IllegalStateException e) {
            bindingResult.reject("signupFailed", "이미 등록된 사용자 입니다.");
            return new ResponseForm<>(HttpStatus.CONFLICT, null, "이미 등록된 사용자 입니다.");
        } catch (Exception e) {
            bindingResult.reject("signupFailed", e.getMessage());
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, e.getMessage());
        }

        // 3. 회원 가입 성공
        return new ResponseForm<>(HttpStatus.OK, userCreateForm, "회원 가입이 성공적으로 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseForm<LoginResponseDTO> loginUser(
            @Validated @RequestBody LoginDTO loginDTO,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, "유효하지 않은 자격 증명입니다.");
        }

        Optional<User> userOptional = userService.findByUsername(loginDTO.getUsername());
        if (userOptional.isEmpty()) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "유효하지 않은 사용자 ID 입니다.");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "유효하지 않은 사용자 비밀번호입니다.");
        }

        try {
            LoginResponseDTO loginResponse = userService.getAccessToken(user, loginDTO.getPassword());
            if (loginResponse == null) {
                throw new Exception("토큰 생성 실패");
            }
            return new ResponseForm<>(HttpStatus.OK, loginResponse, "로그인에 성공했습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "토큰 생성에 실패했습니다. 오류: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseForm<String> logout(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        try {
            userService.logout(username);
            return new ResponseForm<>(HttpStatus.OK, null, "로그아웃에 성공했습니다.");
        } catch (UsernameNotFoundException e) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "유효하지 않은 사용자 ID 입니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "로그아웃에 실패했습니다. 오류: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseForm<LoginResponseDTO> refreshAccessToken(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String refreshToken = request.get("refreshToken");
        try {
            LoginResponseDTO loginResponse = userService.refreshAccessToken(username, refreshToken);
            if (loginResponse == null) {
                return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "유효하지 않은 Refresh Token 입니다.");
            }
            return new ResponseForm<>(HttpStatus.OK, loginResponse, "Access Token 갱신에 성공했습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "토큰 갱신에 실패했습니다. 오류: " + e.getMessage());
        }
    }

    @GetMapping("/protected-resource")
    public ResponseForm<String> getProtectedResource
    (@RequestHeader(value = "Authorization", required = false) String authorization, HttpServletRequest
    request, HttpServletResponse response){
        String token = Optional.ofNullable(authorization).map(auth -> auth.replace("Bearer ", "")).orElse("");
        boolean isTokenValid = userService.validateAccessToken(token);

        if (isTokenValid) {
            return new ResponseForm<>(HttpStatus.OK, "Protected resource accessed successfully!", "Success");
        } else {
            return new ResponseForm<>(HttpStatus.FORBIDDEN, null, "유효하지 않은 토큰입니다.");
        }
    }

    // 유저 조회
    @GetMapping("/{username}")
    public ResponseForm<UserDTO> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return new ResponseForm<>(HttpStatus.OK, user, "User retrieved successfully");
    }

    // 유저 삭제(회원탈퇴)
    @DeleteMapping("/{username}")
    public ResponseForm<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "User deleted successfully");
    }

    // userProfile 수정 또는 생성
    @PatchMapping("/{username}/profile")
    public ResponseForm<ProfileDTO> updateUserProfile(
            @PathVariable String username,
            @RequestBody Map<String, String> updates) {
        String userProfile = updates.get("userProfile");
        String userProfilePic = updates.get("userProfilePic");
        String userNickname = updates.get("userNickname");
        ProfileDTO updatedProfile = userService.updateUserProfile(username, userProfile, userProfilePic, userNickname);
        return new ResponseForm<>(HttpStatus.OK, updatedProfile, "User profile updated successfully");
    }

    // userProfile 삭제
    @DeleteMapping("/{username}/profile")
    public ResponseForm<ProfileDTO> deleteUserProfile(@PathVariable String username) {
        ProfileDTO updatedProfile = userService.deleteUserProfile(username);
        return new ResponseForm<>(HttpStatus.OK, updatedProfile, "User profile deleted successfully");
    }

    // userProfile 조회
    @GetMapping("/{username}/profile")
    public ResponseForm<ProfileDTO> getUserProfile (@PathVariable String username) {
        ProfileDTO userProfile = userService.getUserProfile(username);
        return new ResponseForm<>(HttpStatus.OK, userProfile, "User profile retrieved successfully");
    }


    // 사용자 온도 증가
    @PostMapping("/temperature/increase/{targetUsername}")
    public ResponseForm<UserDTO> increaseUserTemperature(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String targetUsername) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            UserDTO updatedUser = userService.increaseUserTemperature(token, targetUsername);
            return new ResponseForm<>(HttpStatus.OK, updatedUser, targetUsername + "의 온도가 1 상승했습니다.");
        } catch (UnauthorizedException e) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, e.getMessage());
        } catch (RuntimeException e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, e.getMessage());
        }
    }

    // 사용자 온도 감소
    @PostMapping("/temperature/decrease/{targetUsername}")
    public ResponseForm<UserDTO> decreaseUserTemperature(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String targetUsername) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            UserDTO updatedUser = userService.decreaseUserTemperature(token, targetUsername);
            return new ResponseForm<>(HttpStatus.OK, updatedUser, targetUsername + "의 온도가 1 하락했습니다.");
        } catch (UnauthorizedException e) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, e.getMessage());
        } catch (RuntimeException e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, e.getMessage());
        }
    }
}

