package com.mukplay.websocket.service;

import com.mukplay.websocket.constant.StompDestination;
import com.mukplay.websocket.dto.GameEventBroadcast;
import com.mukplay.websocket.dto.GameEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service responsible for broadcasting lifecycle events to /topic/room/{roomId}/event
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameEventBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastEvent(String roomId, GameEventType eventType, Map<String, Object> data) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("eventType은 필수입니다.");
        }

        GameEventBroadcast event = GameEventBroadcast.of(roomId, eventType, data);
        String destination = StompDestination.getRoomEventDestination(roomId);

        messagingTemplate.convertAndSend(destination, event);
        log.info("Game event broadcasted: destination={}, type={}", destination, eventType);
    }
}
