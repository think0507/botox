package com.botox.constant;

import com.botox.domain.User;
import com.botox.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitalizer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitalizer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        //이미 존재 하는지 확인
        if(userRepository.existsByUsername("admin")){
            return;
        }

        //새로운 관리자 계정 생성
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("adminpassword")); // 원하는 비밀번호 설정
        adminUser.setUserNickname("Admin");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setStatus(UserStatus.ONLINE);

        userRepository.save(adminUser);
    }
}
