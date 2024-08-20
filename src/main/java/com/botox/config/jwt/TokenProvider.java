package com.botox.config.jwt;

import com.botox.domain.User;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static javax.crypto.Cipher.SECRET_KEY;
import static org.kurento.jsonrpc.client.JsonRpcClient.log;

@Service
@RequiredArgsConstructor
public class TokenProvider {
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private Key getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String generateAccessToken(User user, Duration expiry) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + expiry.toMillis());
        return makeToken(now, expiredAt, user);
    }

    public String generateRefreshToken(User user, Duration expiry) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + expiry.toMillis());
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setExpiration(expiredAt)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private String makeToken(Date now, Date expiredAt, User user) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiredAt)
                .setSubject(user.getUsername())
                .claim("username", user.getUsername())
                .claim("user_nickname", user.getUserNickname())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.emptySet();
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
    }

    // 게스트용 토큰 생성
    public String generateGuestToken(Long roomNum) {
        // UUID와 토큰은 한 번만 생성
        String uuid = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1시간 후 만료

        // 토큰 생성
        String token = Jwts.builder()
                .setSubject("GUEST_TOKEN")
                .claim("roomNum", roomNum)
                .claim("uuid", uuid)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        // Redis에 한 번만 저장
        String redisKey = "GUEST_TOKEN:" + roomNum + ":" + uuid;
        redisTemplate.opsForValue().set(redisKey, token, Duration.ofDays(1));

        return token;
    }

    // 게스트용 토큰에서 방 번호 추출
    public Long getRoomNumFromGuestToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("roomNum", Long.class);
    }

    // 게스트용 토큰에서 UUID 추출
    public String getUUIDFromGuestToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String uuid = claims.get("uuid", String.class);
            if (uuid == null) {
                throw new IllegalStateException("UUID 클레임이 토큰에 없습니다.");
            }

            return uuid;
        } catch (JwtException e) {
            log.error("토큰 파싱 중 오류 발생: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.", e);
        }
    }
}
