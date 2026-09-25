package com.mukplay.websocket;

import com.mukplay.websocket.constant.StompDestination;
import com.mukplay.websocket.dto.GameEventBroadcast;
import com.mukplay.websocket.dto.GameEventType;
import com.mukplay.websocket.service.GameEventBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GameEventTest {

    private SimpMessagingTemplate messagingTemplate;
    private GameEventBroadcastService eventBroadcastService;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        eventBroadcastService = new GameEventBroadcastService(messagingTemplate);
    }

    @Test
    @DisplayName("게임 라이프사이클 이벤트 5종(GAME_STARTED, ROUND_STARTED, ROUND_ENDED, PLAYER_ELIMINATED, GAME_FINISHED)이 올바른 목적지로 브로드캐스트된다")
    void broadcastLifecycleEvents() {
        String roomId = "event-room-99";

        // 1. GAME_STARTED
        eventBroadcastService.broadcastEvent(roomId, GameEventType.GAME_STARTED, Map.of("maxRounds", 5));
        // 2. ROUND_STARTED
        eventBroadcastService.broadcastEvent(roomId, GameEventType.ROUND_STARTED, Map.of("round", 1, "questionId", 123L));
        // 3. ROUND_ENDED
        eventBroadcastService.broadcastEvent(roomId, GameEventType.ROUND_ENDED, Map.of("round", 1, "answer", "O"));
        // 4. PLAYER_ELIMINATED
        eventBroadcastService.broadcastEvent(roomId, GameEventType.PLAYER_ELIMINATED, Map.of("eliminatedUserIds", java.util.List.of(10L, 20L)));
        // 5. GAME_FINISHED
        eventBroadcastService.broadcastEvent(roomId, GameEventType.GAME_FINISHED, Map.of("winnerUserId", 30L));

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GameEventBroadcast> payloadCaptor = ArgumentCaptor.forClass(GameEventBroadcast.class);

        verify(messagingTemplate, times(5)).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        assertThat(destinationCaptor.getAllValues()).allMatch(dest -> dest.equals(StompDestination.getRoomEventDestination(roomId)));

        java.util.List<GameEventBroadcast> payloads = payloadCaptor.getAllValues();
        assertThat(payloads.get(0).eventType()).isEqualTo(GameEventType.GAME_STARTED);
        assertThat(payloads.get(1).eventType()).isEqualTo(GameEventType.ROUND_STARTED);
        assertThat(payloads.get(2).eventType()).isEqualTo(GameEventType.ROUND_ENDED);
        assertThat(payloads.get(3).eventType()).isEqualTo(GameEventType.PLAYER_ELIMINATED);
        assertThat(payloads.get(4).eventType()).isEqualTo(GameEventType.GAME_FINISHED);
    }
}
