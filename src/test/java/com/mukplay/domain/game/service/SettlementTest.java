package com.mukplay.domain.game.service;

import com.mukplay.domain.game.dto.PlayerSettlementInfo;
import com.mukplay.domain.game.entity.GameLog;
import com.mukplay.domain.game.entity.GameResult;
import com.mukplay.domain.game.repository.GameLogRepository;
import com.mukplay.domain.game.repository.GameResultRepository;
import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.entity.UserRole;
import com.mukplay.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
class SettlementTest {

    @Mock
    private GameLogRepository gameLogRepository;

    @Mock
    private GameResultRepository gameResultRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private ExpCalculatorService expCalculatorService = new ExpCalculatorService();

    @InjectMocks
    private GameSettlementService settlementService;

    @Test
    @DisplayName("게임 종료 시 GameLog, GameResult 가 생성되고 플레이어 경험치 및 레벨업이 정상 반영된다")
    void settleGameAndLevelUp() {
        String roomId = "settle-room-1";
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(3);
        LocalDateTime finishedAt = LocalDateTime.now();

        // 1위 유저 (경험치 80 보유)
        User winner = User.builder().loginId("winner").password("pass").nickname("승자").role(UserRole.ROLE_USER).build();
        ReflectionTestUtils.setField(winner, "id", 1L);
        winner.addExp(80); // exp=80, level=1

        // 2위 유저
        User runnerUp = User.builder().loginId("runner").password("pass").nickname("패자").role(UserRole.ROLE_USER).build();
        ReflectionTestUtils.setField(runnerUp, "id", 2L);

        given(gameLogRepository.findByRoomId(roomId)).willReturn(Optional.empty());

        GameLog savedLog = GameLog.builder()
                .roomId(roomId)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .participantCount(2)
                .build();
        ReflectionTestUtils.setField(savedLog, "id", 100L);
        given(gameLogRepository.save(any(GameLog.class))).willReturn(savedLog);

        given(userRepository.findById(1L)).willReturn(Optional.of(winner));
        given(userRepository.findById(2L)).willReturn(Optional.of(runnerUp));

        given(gameResultRepository.save(any(GameResult.class))).willAnswer(inv -> inv.getArgument(0));

        // 1위: 기본 50 + 보너스 (2-1)*10 = 10 => 60 EXP 획득 (총 80 + 60 = 140 => 레벨 2 달성)
        PlayerSettlementInfo p1 = new PlayerSettlementInfo(1L, 1, 3, 50, true);
        // 2위: 기본 20 EXP 획득 => 총 20 => 레벨 1 유지
        PlayerSettlementInfo p2 = new PlayerSettlementInfo(2L, 2, 1, 20, false);

        List<GameResult> results = settlementService.settleGame(roomId, startedAt, finishedAt, 2, List.of(p1, p2));

        assertThat(results).hasSize(2);

        // 승자 검증
        assertThat(winner.getExp()).isEqualTo(140);
        assertThat(winner.getLevel()).isEqualTo(2);

        // 패자 검증
        assertThat(runnerUp.getExp()).isEqualTo(20);
        assertThat(runnerUp.getLevel()).isEqualTo(1);

        verify(gameLogRepository, times(1)).save(any(GameLog.class));
        verify(gameResultRepository, times(2)).save(any(GameResult.class));
    }
}
