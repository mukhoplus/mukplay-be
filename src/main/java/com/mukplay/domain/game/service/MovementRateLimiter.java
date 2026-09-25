package com.mukplay.domain.game.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Rate limiter using Redis sliding window/token bucket or fixed window counter
 * to prevent movement spamming.
 * e.g., max 20 movements per second per player.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MovementRateLimiter {

    private final StringRedisTemplate redisTemplate;

    public static final int MAX_REQUESTS_PER_SECOND = 20;
    private static final String RATE_LIMIT_KEY_PREFIX = "rate:move:";

    public boolean isAllowed(Long userId) {
        if (userId == null) {
            return false;
        }

        long currentSecond = System.currentTimeMillis() / 1000;
        String key = RATE_LIMIT_KEY_PREFIX + userId + ":" + currentSecond;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(2));
        }

        if (count != null && count > MAX_REQUESTS_PER_SECOND) {
            log.warn("Rate limit exceeded for user: userId={}, count={}", userId, count);
            return false;
        }

        return true;
    }
}
