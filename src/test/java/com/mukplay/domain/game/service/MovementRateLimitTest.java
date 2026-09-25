package com.mukplay.domain.game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class MovementRateLimitTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private MovementRateLimiter rateLimiter;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        rateLimiter = new MovementRateLimiter(redisTemplate);
    }

    @Test
    @DisplayName("초당 20회 이하의 이동 요청은 허용된다")
    void allowUnderRateLimit() {
        Long userId = 123L;
        given(valueOperations.increment(anyString())).willReturn(1L, 10L, 20L);

        assertThat(rateLimiter.isAllowed(userId)).isTrue();
        assertThat(rateLimiter.isAllowed(userId)).isTrue();
        assertThat(rateLimiter.isAllowed(userId)).isTrue();

        // 첫 번째 호출 시 TTL 설정 확인
        verify(redisTemplate, times(1)).expire(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("초당 20회를 초과하는 이동 스팸 요청은 거부(차단)된다")
    void blockSpamOverRateLimit() {
        Long userId = 456L;
        given(valueOperations.increment(anyString())).willReturn(21L, 22L);

        assertThat(rateLimiter.isAllowed(userId)).isFalse();
        assertThat(rateLimiter.isAllowed(userId)).isFalse();
    }
}
