package com.taskapi.taskapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public void blacklist(String token, long expirationMill){
        log.info("Blacklisting token, TTL: {}ms", expirationMill);
        redisTemplate.opsForValue().set(
                "blacklist:" + token,
                "true",
                expirationMill,
                TimeUnit.MILLISECONDS);
        log.info("Token blacklisted successfully");
    }

    public boolean isBlacklisted(String token){
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:"+ token));
    }
}
