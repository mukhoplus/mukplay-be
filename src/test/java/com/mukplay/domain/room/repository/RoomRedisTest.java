package com.mukplay.domain.room.repository;

import com.mukplay.domain.room.model.Room;
import com.mukplay.domain.room.model.RoomParticipant;
import com.mukplay.domain.room.model.RoomState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RoomRedisTest {

    @Autowired
    private RoomRedisRepository roomRedisRepository;

    private static final String TEST_ROOM_ID = "test-redis-room-01";

    @AfterEach
    void tearDown() {
        roomRedisRepository.deleteById(TEST_ROOM_ID);
    }

    @Test
    @DisplayName("Room을 Redis에 저장하고 다시 조회할 수 있어야 한다")
    void testSaveAndFindRoom() {
        Room room = Room.builder()
                .roomId(TEST_ROOM_ID)
                .name("Redis 방 테스트")
                .hostId(10L)
                .maxPlayers(8)
                .build();
        room.addParticipant(new RoomParticipant(10L, "호스트유저"));
        room.addParticipant(new RoomParticipant(20L, "게스트유저"));

        roomRedisRepository.save(room);

        Optional<Room> found = roomRedisRepository.findById(TEST_ROOM_ID);
        assertThat(found).isPresent();
        Room retrieved = found.get();
        assertThat(retrieved.getName()).isEqualTo("Redis 방 테스트");
        assertThat(retrieved.getHostId()).isEqualTo(10L);
        assertThat(retrieved.getMaxPlayers()).isEqualTo(8);
        assertThat(retrieved.getState()).isEqualTo(RoomState.WAITING);
        assertThat(retrieved.getParticipants()).hasSize(2);
    }

    @Test
    @DisplayName("findAllWaiting 호출 시 WAITING 상태인 방만 반환해야 한다")
    void testFindAllWaiting() {
        Room room = Room.builder()
                .roomId(TEST_ROOM_ID)
                .name("대기방")
                .hostId(1L)
                .maxPlayers(5)
                .build();
        room.addParticipant(new RoomParticipant(1L, "방장"));
        roomRedisRepository.save(room);

        List<Room> waitingRooms = roomRedisRepository.findAllWaiting();
        assertThat(waitingRooms).anyMatch(r -> r.getRoomId().equals(TEST_ROOM_ID));
    }

    @Test
    @DisplayName("deleteById 호출 시 Redis에서 방이 삭제되어야 한다")
    void testDeleteRoom() {
        Room room = Room.builder()
                .roomId(TEST_ROOM_ID)
                .name("삭제될 방")
                .hostId(1L)
                .maxPlayers(5)
                .build();
        roomRedisRepository.save(room);

        roomRedisRepository.deleteById(TEST_ROOM_ID);

        Optional<Room> found = roomRedisRepository.findById(TEST_ROOM_ID);
        assertThat(found).isEmpty();
    }
}
