package com.mukplay.domain.room.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mukplay.domain.room.model.Room;
import com.mukplay.domain.room.model.RoomState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RoomRedisRepository {

    private static final String ROOM_KEY_PREFIX = "room:";
    private static final String ROOM_INDEX_KEY = "rooms:index";
    private static final Duration ROOM_TTL = Duration.ofHours(3);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void save(Room room) {
        try {
            String key = ROOM_KEY_PREFIX + room.getRoomId();
            RoomRedisDto dto = RoomRedisDto.from(room);
            String json = objectMapper.writeValueAsString(dto);

            stringRedisTemplate.opsForValue().set(key, json, ROOM_TTL);
            stringRedisTemplate.opsForSet().add(ROOM_INDEX_KEY, room.getRoomId());
        } catch (Exception e) {
            throw new RuntimeException("방 상태 Redis 저장 실패: " + e.getMessage(), e);
        }
    }

    public Optional<Room> findById(String roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            stringRedisTemplate.opsForSet().remove(ROOM_INDEX_KEY, roomId);
            return Optional.empty();
        }

        try {
            RoomRedisDto dto = objectMapper.readValue(json, RoomRedisDto.class);
            return Optional.of(dto.toDomain());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Room> findAllWaiting() {
        Set<String> roomIds = stringRedisTemplate.opsForSet().members(ROOM_INDEX_KEY);
        if (roomIds == null || roomIds.isEmpty()) {
            return List.of();
        }

        List<Room> result = new ArrayList<>();
        for (String id : roomIds) {
            findById(id).ifPresent(room -> {
                if (room.getState() == RoomState.WAITING) {
                    result.add(room);
                }
            });
        }
        return result;
    }

    public void deleteById(String roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForSet().remove(ROOM_INDEX_KEY, roomId);
    }
}
