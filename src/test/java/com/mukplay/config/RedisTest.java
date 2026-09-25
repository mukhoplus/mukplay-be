package com.mukplay.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("StringRedisTemplate set -> get -> delete 작업이 정상 동작해야 한다")
    void testStringRedisOperations() {
        String key = "test:string:key";
        String value = "hello-redis";

        stringRedisTemplate.opsForValue().set(key, value, 10, TimeUnit.SECONDS);

        String retrieved = stringRedisTemplate.opsForValue().get(key);
        assertThat(retrieved).isEqualTo(value);

        Boolean deleted = stringRedisTemplate.delete(key);
        assertThat(deleted).isTrue();

        String afterDelete = stringRedisTemplate.opsForValue().get(key);
        assertThat(afterDelete).isNull();
    }

    @Test
    @DisplayName("RedisTemplate 객체 직렬화 set -> get -> delete 작업이 정상 동작해야 한다")
    void testObjectRedisOperations() {
        String key = "test:object:key";
        TestPayload payload = new TestPayload(100L, "mukplay-test", true);

        redisTemplate.opsForValue().set(key, payload, 10, TimeUnit.SECONDS);

        Object retrievedObj = redisTemplate.opsForValue().get(key);
        assertThat(retrievedObj).isNotNull();

        Boolean deleted = redisTemplate.delete(key);
        assertThat(deleted).isTrue();
    }

    public record TestPayload(Long id, String name, boolean active) {
    }
}
