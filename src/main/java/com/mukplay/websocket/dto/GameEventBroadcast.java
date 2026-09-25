package com.mukplay.websocket.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Payload broadcasted to /topic/room/{roomId}/event
 */
public record GameEventBroadcast(
        String roomId,
        GameEventType eventType,
        long timestamp,
        Map<String, Object> data
) {
    public static GameEventBroadcast of(String roomId, GameEventType eventType, Map<String, Object> data) {
        return new GameEventBroadcast(
                roomId,
                eventType,
                Instant.now().toEpochMilli(),
                data != null ? data : Map.of()
        );
    }
}
