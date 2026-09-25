package com.mukplay.domain.game.service;

import com.mukplay.domain.game.dto.PlayerSettlementInfo;
import com.mukplay.domain.game.entity.GameLog;
import com.mukplay.domain.game.entity.GameResult;
import com.mukplay.domain.game.repository.GameLogRepository;
import com.mukplay.domain.game.repository.GameResultRepository;
import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotentSettlementTest {

    @Mock
    private GameLogRepository gameLogRepository;

    @Mock
    private GameResultRepository gameResultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpCalculatorService expCalculatorService;

    @InjectMocks
    private GameSettlementService settlementService;

    @Test
    @DisplayName("이미 정산이 완료된 방(roomId)에 대해 중복 정산 요청 시 추가 저장 및 경험치 중복 지급 없이 기존 결과를 반환한다")
    void duplicateSettlementIsIgnored() {
        String roomId = "idempotent-room-99";
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime finishedAt = LocalDateTime.now();

        GameLog existingLog = GameLog.builder()
                .roomId(roomId)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .participantCount(3)
                .build();
        ReflectionTestUtils.setField(existingLog, "id", 555L);

        GameResult existingResult = GameResult.builder()
                .gameLogId(555L)
                .userId(1L)
                .rank(1)
                .earnedExp(50)
                .survivedRounds(3)
                .build();

        // 1. 이미 GameLog가 존재하는 상황 모킹
        given(gameLogRepository.findByRoomId(roomId)).willReturn(Optional.of(existingLog));
        given(gameResultRepository.findByGameLogId(555L)).willReturn(List.of(existingResult));

        PlayerSettlementInfo playerInfo = new PlayerSettlementInfo(1L, 1, 3, 50, true);

        // 정산 재호출
        List<GameResult> results = settlementService.settleGame(
                roomId, startedAt, finishedAt, 3, List.of(playerInfo)
        );

        // 검증: 기존 결과 반환
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getGameLogId()).isEqualTo(555L);

        // 추가 저장이 절대 호출되지 않아야 함 (멱등성 보장)
        verify(gameLogRepository, never()).save(any(GameLog.class));
        verify(gameResultRepository, never()).save(any(GameResult.class));
        verify(userRepository, never()).findById(anyLong());
        verify(expCalculatorService, never()).calculateTotalExp(anyInt(), anyBoolean(), anyInt());
    }
}
