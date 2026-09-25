package com.mukplay.websocket;

import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.websocket.constant.StompDestination;
import com.mukplay.websocket.dto.RoomPositionsBroadcast;
import com.mukplay.websocket.service.GamePositionBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PositionBroadcastTest {

    private SimpMessagingTemplate messagingTemplate;
    private GamePositionBroadcastService positionBroadcastService;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        positionBroadcastService = new GamePositionBroadcastService(messagingTemplate);
    }

    @Test
    @DisplayName("게임 세션 내 모든 플레이어의 위치 정보가 /topic/room/{roomId}/positions 로 브로드캐스트된다")
    void broadcastPlayerPositions() {
        String roomId = "room-pos-1";
        GameSession session = new GameSession(roomId, 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState p1 = new PlayerState(101L, 25.0, 40.0);
        PlayerState p2 = new PlayerState(102L, 85.0, 60.0);
        session.addPlayer(p1);
        session.addPlayer(p2);

        positionBroadcastService.broadcastPositions(session);

        ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RoomPositionsBroadcast> payloadCaptor = ArgumentCaptor.forClass(RoomPositionsBroadcast.class);

        verify(messagingTemplate, times(1)).convertAndSend(destCaptor.capture(), payloadCaptor.capture());

        assertThat(destCaptor.getValue()).isEqualTo(StompDestination.getRoomPositionsDestination(roomId));

        RoomPositionsBroadcast payload = payloadCaptor.getValue();
        assertThat(payload.roomId()).isEqualTo(roomId);
        assertThat(payload.timestamp()).isGreaterThan(0);
        assertThat(payload.positions()).hasSize(2);

        RoomPositionsBroadcast.PlayerPositionDto p1Dto = payload.positions().stream()
                .filter(p -> p.userId().equals(101L))
                .findFirst()
                .orElseThrow();
        assertThat(p1Dto.x()).isEqualTo(25.0);
        assertThat(p1Dto.y()).isEqualTo(40.0);
        assertThat(p1Dto.alive()).isTrue();
    }
}
