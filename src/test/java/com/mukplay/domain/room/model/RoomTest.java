package com.mukplay.domain.room.model;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTest {

    @Test
    @DisplayName("방 생성 시 WAITING 상태이고 정원 검증이 정상 동작해야 한다")
    void testRoomCreation() {
        Room room = Room.builder()
                .roomId("room-1")
                .name("즐거운 OX 퀴즈")
                .hostId(1L)
                .maxPlayers(5)
                .build();

        assertThat(room.getState()).isEqualTo(RoomState.WAITING);
        assertThat(room.getMaxPlayers()).isEqualTo(5);
        assertThat(room.getParticipants()).isEmpty();
    }

    @Test
    @DisplayName("정원 초과 시 참가 요청은 ROOM_FULL 예외를 던져야 한다")
    void testRoomCapacityExceeded() {
        Room room = Room.builder()
                .roomId("room-2")
                .name("소규모 방")
                .hostId(1L)
                .maxPlayers(2)
                .build();

        room.addParticipant(new RoomParticipant(1L, "호스트"));
        room.addParticipant(new RoomParticipant(2L, "참가자1"));

        assertThatThrownBy(() -> room.addParticipant(new RoomParticipant(3L, "참가자2")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_FULL);
    }

    @Test
    @DisplayName("이미 참여 중인 유저의 중복 참가는 ALREADY_JOINED 예외를 던져야 한다")
    void testDuplicateJoinRejected() {
        Room room = Room.builder()
                .roomId("room-3")
                .name("중복 테스트")
                .hostId(1L)
                .maxPlayers(10)
                .build();

        room.addParticipant(new RoomParticipant(1L, "호스트"));

        assertThatThrownBy(() -> room.addParticipant(new RoomParticipant(1L, "호스트중복")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_JOINED);
    }

    @Test
    @DisplayName("방장이 퇴장하면 다음 참가자에게 방장이 자동으로 위임되어야 한다")
    void testHostMigrationOnLeave() {
        Room room = Room.builder()
                .roomId("room-4")
                .name("위임 테스트")
                .hostId(1L)
                .maxPlayers(10)
                .build();

        room.addParticipant(new RoomParticipant(1L, "원래방장"));
        room.addParticipant(new RoomParticipant(2L, "다음방장"));

        room.removeParticipant(1L);

        assertThat(room.getHostId()).isEqualTo(2L);
        assertThat(room.getParticipants()).hasSize(1);
    }

    @Test
    @DisplayName("방장이 아니거나 2명 미만일 때 게임 시작 시 예외가 발생해야 한다")
    void testStartValidation() {
        Room room = Room.builder()
                .roomId("room-5")
                .name("시작 검증")
                .hostId(1L)
                .maxPlayers(10)
                .build();

        room.addParticipant(new RoomParticipant(1L, "방장"));

        // 혼자서는 시작 불가
        assertThatThrownBy(() -> room.start(1L))
                .isInstanceOf(BusinessException.class);

        room.addParticipant(new RoomParticipant(2L, "참가자2"));

        // 방장이 아닌 사람이 시작 요청 시 불가
        assertThatThrownBy(() -> room.start(2L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_ROOM_HOST);

        // 방장이 시작하면 정상 시작
        room.start(1L);
        assertThat(room.getState()).isEqualTo(RoomState.PLAYING);
    }
}
