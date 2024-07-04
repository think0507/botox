package com.botox.controller;

import com.botox.domain.UserCreateForm;
import com.botox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseForm<UserCreateForm> createUser(
            @Validated @RequestBody UserCreateForm userCreateForm,
            BindingResult bindingResult
    ) {
        // 1. Form 데이터 검증
        // (1-1) 입력값 바인딩 검사
        if (bindingResult.hasErrors()) {
            return new ResponseForm<>(
                    HttpStatus.BAD_REQUEST,
                    null,
                    "입력값에 오류가 있습니다."
            );
        }
        // (1-2) 입력값 내용 검사
        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue(
                    "password2",
                    "passwordDoubleCheckError",
                    "패스워드 확인 값이 일치하지 않습니다."
            );
            return new ResponseForm<>(
                    HttpStatus.BAD_REQUEST,
                    null,
                    "패스워드 확인 값이 일치하지 않습니다."
            );
        }

        // 2. 백엔드 validation
        try {
            userService.createUser(
                    userCreateForm.getUsername(),
                    userCreateForm.getPassword1(),
                    userCreateForm.getEmail()
            );
        } catch (IllegalStateException e) {
            bindingResult.reject(
                    "signupFailed",
                    "이미 등록된 사용자 입니다."
            );
            return new ResponseForm<>(
                    HttpStatus.CONFLICT,
                    null,
                    "이미 등록된 사용자 입니다."
            );
        } catch (Exception e) {
            bindingResult.reject(
                    "signupFailed",
                    e.getMessage()
            );
            return new ResponseForm<>(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    null,
                    e.getMessage()
            );
        }

        // 3. 회원 가입 성공
        return new ResponseForm<>(
                HttpStatus.OK,
                userCreateForm,
                "회원 가입이 성공적으로 완료되었습니다."
        );
    }
}
