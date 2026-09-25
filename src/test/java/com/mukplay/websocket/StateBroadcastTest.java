package com.mukplay.websocket;

import com.mukplay.domain.game.model.*;
import com.mukplay.websocket.constant.StompDestination;
import com.mukplay.websocket.dto.GameStateBroadcast;
import com.mukplay.websocket.service.GameStateBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StateBroadcastTest {

    private SimpMessagingTemplate messagingTemplate;
    private GameStateBroadcastService broadcastService;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        broadcastService = new GameStateBroadcastService(messagingTemplate);
    }

    @Test
    @DisplayName("게임 상태 및 현재 라운드 정보가 /topic/room/{roomId}/state 로 올바르게 브로드캐스트된다")
    void broadcastStateUpdates() {
        String roomId = "room-broadcast-1";
        GameSession session = new GameSession(roomId, 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);
        session.nextRound();

        session.addPlayer(new PlayerState(1L, 30.0, 50.0));
        session.addPlayer(new PlayerState(2L, 70.0, 50.0));

        Instant startTime = Instant.parse("2026-09-26T00:00:00Z");
        Round round = new Round(1, 1001L, Answer.O, startTime, Duration.ofSeconds(10));

        broadcastService.broadcastState(session, round);

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GameStateBroadcast> payloadCaptor = ArgumentCaptor.forClass(GameStateBroadcast.class);

        verify(messagingTemplate, times(1)).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        assertThat(destinationCaptor.getValue()).isEqualTo(StompDestination.getRoomStateDestination(roomId));

        GameStateBroadcast payload = payloadCaptor.getValue();
        assertThat(payload.roomId()).isEqualTo(roomId);
        assertThat(payload.state()).isEqualTo(GameSessionState.PLAYING);
        assertThat(payload.currentRound()).isEqualTo(1);
        assertThat(payload.maxRounds()).isEqualTo(5);
        assertThat(payload.questionId()).isEqualTo(1001L);
        assertThat(payload.startedAt()).isEqualTo(startTime);
        assertThat(payload.endsAt()).isEqualTo(startTime.plusSeconds(10));
        assertThat(payload.alivePlayerCount()).isEqualTo(2);
    }
}
