package com.botox.config.jwt;

import com.botox.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TokenProvider {
    private final JwtProperties jwtProperties;

    public String generateToken(User user, Duration expiry) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + expiry.toMillis());
        return makeToken(now, expiredAt, user);
    }

    // 토큰을 내부에서 생성
    private String makeToken(Date now, Date expiredAt, User user) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration (expiredAt)
                .setSubject(user.getUserNickname())
                .claim("id", user.getId())
                // 암호키를 사용해서 시그니처를 작성
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

    // 토큰을 외부에서 수신 후 검증
    public boolean validateToken(String token) {
        // 인코딩된 내용, 암호화된 signature 모두 의미 추출
        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())  // 암호키를 알고있는 서버가
                    .parseClaimsJws(token);                       // 토큰을 해석한다
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰 수신 후 토큰 소유자 조회
    public Authentication getAuthentication(String token) {
        // 토큰 정보를 통해 유저 인증 정보 확인
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        return new UsernamePasswordAuthenticationToken(
                claims.getSubject(),
                token,
                authorities
        );
    }

    private Claims getClaims(String token) {
        // 토큰 기반 클레임 데이터 해독
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }
}
