package com.botox.service;

import com.sun.net.httpserver.Authenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisLockService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 자원에 대한 잠금 키
    private static final String LOCK_KEY = "LOCK_KEY";

    public boolean acquireLock(String key, Duration timeout) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, key, timeout);
        return success != null && success;
    }

    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}
