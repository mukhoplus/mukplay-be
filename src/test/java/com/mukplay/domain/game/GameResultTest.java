package com.mukplay.domain.game;

import com.mukplay.domain.game.entity.GameResult;
import com.mukplay.domain.game.repository.GameResultRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GameResultTest {

    @Mock
    private GameResultRepository gameResultRepository;

    @Test
    @DisplayName("GameResult 엔티티를 생성하고 저장 및 조회가 정상 수행된다")
    void createAndPersistGameResult() {
        GameResult result = GameResult.builder()
                .gameLogId(10L)
                .userId(1L)
                .rank(1)
                .earnedExp(120)
                .survivedRounds(5)
                .build();

        given(gameResultRepository.save(any(GameResult.class))).willReturn(result);
        given(gameResultRepository.findByGameLogIdAndUserId(10L, 1L)).willReturn(Optional.of(result));
        given(gameResultRepository.findByUserId(1L)).willReturn(List.of(result));

        GameResult saved = gameResultRepository.save(result);
        assertThat(saved.getGameLogId()).isEqualTo(10L);
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getRank()).isEqualTo(1);
        assertThat(saved.getEarnedExp()).isEqualTo(120);
        assertThat(saved.getSurvivedRounds()).isEqualTo(5);

        Optional<GameResult> found = gameResultRepository.findByGameLogIdAndUserId(10L, 1L);
        assertThat(found).isPresent();
        assertThat(found.get().getEarnedExp()).isEqualTo(120);

        List<GameResult> userResults = gameResultRepository.findByUserId(1L);
        assertThat(userResults).hasSize(1);
    }

    @Test
    @DisplayName("필수 필드(gameLogId, userId, rank) 누락 시 예외가 발생한다")
    void validateMandatoryFields() {
        assertThatThrownBy(() -> GameResult.builder()
                .gameLogId(null)
                .userId(1L)
                .rank(1)
                .earnedExp(10)
                .survivedRounds(1)
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> GameResult.builder()
                .gameLogId(1L)
                .userId(null)
                .rank(1)
                .earnedExp(10)
                .survivedRounds(1)
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> GameResult.builder()
                .gameLogId(1L)
                .userId(1L)
                .rank(0)
                .earnedExp(10)
                .survivedRounds(1)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
