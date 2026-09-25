package com.mukplay.domain.game;

import com.mukplay.domain.game.entity.GameLog;
import com.mukplay.domain.game.repository.GameLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GameLogTest {

    @Mock
    private GameLogRepository gameLogRepository;

    @Test
    @DisplayName("GameLog 엔티티를 생성하고 저장 및 조회가 정상 수행된다")
    void createAndPersistGameLog() {
        LocalDateTime now = LocalDateTime.now();
        GameLog gameLog = GameLog.builder()
                .roomId("room-log-100")
                .startedAt(now.minusMinutes(5))
                .finishedAt(now)
                .participantCount(8)
                .build();

        given(gameLogRepository.save(any(GameLog.class))).willReturn(gameLog);
        given(gameLogRepository.findByRoomId("room-log-100")).willReturn(Optional.of(gameLog));

        GameLog saved = gameLogRepository.save(gameLog);
        assertThat(saved.getRoomId()).isEqualTo("room-log-100");
        assertThat(saved.getParticipantCount()).isEqualTo(8);

        Optional<GameLog> found = gameLogRepository.findByRoomId("room-log-100");
        assertThat(found).isPresent();
        assertThat(found.get().getStartedAt()).isEqualTo(now.minusMinutes(5));
    }

    @Test
    @DisplayName("필수 필드(roomId, startedAt, finishedAt) 누락 시 예외가 발생한다")
    void validateMandatoryFields() {
        LocalDateTime now = LocalDateTime.now();

        assertThatThrownBy(() -> GameLog.builder()
                .roomId(null)
                .startedAt(now)
                .finishedAt(now)
                .participantCount(5)
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> GameLog.builder()
                .roomId("room-1")
                .startedAt(null)
                .finishedAt(now)
                .participantCount(5)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
