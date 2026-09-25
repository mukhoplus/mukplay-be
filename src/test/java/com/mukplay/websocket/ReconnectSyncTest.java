package com.mukplay.websocket;

import com.mukplay.domain.game.model.*;
import com.mukplay.websocket.dto.GameSyncSnapshot;
import com.mukplay.websocket.service.GameReconnectSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReconnectSyncTest {

    private SimpMessagingTemplate messagingTemplate;
    private GameReconnectSyncService syncService;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        syncService = new GameReconnectSyncService(messagingTemplate);
    }

    @Test
    @DisplayName("재접속 시 현재 게임 상태(진행 상태, 라운드, 잔여 시간, 모든 플레이어 위치 및 생존 여부) 스냅샷을 생성하여 전송한다")
    void createAndSendSnapshotOnReconnect() {
        String roomId = "reconnect-room-1";
        GameSession session = new GameSession(roomId, 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);
        session.nextRound(); // round 1

        PlayerState p1 = new PlayerState(10L, 20.0, 30.0);
        PlayerState p2 = new PlayerState(20L, 80.0, 70.0);
        p2.eliminate(); // p2는 탈락 상태

        session.addPlayer(p1);
        session.addPlayer(p2);

        Instant roundStart = Instant.parse("2026-09-26T00:00:00Z");
        Round round = new Round(1, 999L, Answer.X, roundStart, Duration.ofSeconds(10));

        GameSyncSnapshot snapshot = syncService.createSnapshot(session, round);

        assertThat(snapshot.roomId()).isEqualTo(roomId);
        assertThat(snapshot.state()).isEqualTo(GameSessionState.PLAYING);
        assertThat(snapshot.currentRound()).isEqualTo(1);
        assertThat(snapshot.maxRounds()).isEqualTo(5);
        assertThat(snapshot.questionId()).isEqualTo(999L);
        assertThat(snapshot.roundStartedAt()).isEqualTo(roundStart);
        assertThat(snapshot.roundEndsAt()).isEqualTo(roundStart.plusSeconds(10));
        assertThat(snapshot.positions()).hasSize(2);

        // p2 탈락 상태 유지 확인
        assertThat(snapshot.positions().stream().filter(p -> p.userId().equals(20L)).findFirst().get().alive())
                .isFalse();

        // 전송 검증
        syncService.sendSyncSnapshotToUser("client-session-123", session, round);
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq("client-session-123"),
                eq("/queue/sync"),
                any(GameSyncSnapshot.class)
        );
    }
}
