package com.botox.domain;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collections;

public class SpringUser extends User {  // Wrapper 로 작용
    public SpringUser(String username, String password) {
        super(username, password, true, true, true, true, Collections.emptyList());
    }

    public SpringUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, Collections.emptyList());
    }

    public static UserDetails getSpringUserDetails(com.botox.domain.User appUser) {
        return User.builder()
                .username(appUser.getUserId())
                .password(appUser.getPassword())
                .build();
    }
}
