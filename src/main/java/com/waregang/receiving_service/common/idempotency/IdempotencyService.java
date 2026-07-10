package com.waregang.receiving_service.common.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final Duration KEY_EXPIRATION = Duration.ofHours(1);
    private final StringRedisTemplate redisTemplate;

    /**
     * Tries to acquire a lock for a given idempotency key.
     *
     * @param key the idempotency key
     * @return {@code true} if the lock was acquired, {@code false} otherwise
     */
    public boolean tryLock(String key) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", KEY_EXPIRATION));
    }
}