package com.mukplay.websocket;

import com.mukplay.websocket.config.WebSocketConfig;
import com.mukplay.websocket.constant.StompDestination;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StompDestinationTest {

    @Test
    @DisplayName("STOMP 구독 및 발행 목적지 URL 템플릿이 규약에 맞게 생성된다")
    void testDestinationGeneration() {
        String roomId = "test-room-123";

        assertThat(StompDestination.getRoomStateDestination(roomId))
                .isEqualTo("/topic/room/test-room-123/state");
        assertThat(StompDestination.getRoomPositionsDestination(roomId))
                .isEqualTo("/topic/room/test-room-123/positions");
        assertThat(StompDestination.getRoomEventDestination(roomId))
                .isEqualTo("/topic/room/test-room-123/event");
        assertThat(StompDestination.APP_MOVE)
                .isEqualTo("/game/move");
    }

    @Test
    @DisplayName("WebSocketConfig가 /topic 브로커 및 /app 프리픽스를 올바르게 구성한다")
    void testWebSocketConfigBrokerRegistration() {
        WebSocketConfig config = new WebSocketConfig(null);
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        config.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/topic");
        verify(registry).setApplicationDestinationPrefixes("/app");
    }
}
