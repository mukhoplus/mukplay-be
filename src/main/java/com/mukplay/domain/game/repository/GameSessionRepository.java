package com.mukplay.domain.game.repository;

import com.mukplay.domain.game.model.GameSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for active GameSessions during live gameplay.
 */
@Component
public class GameSessionRepository {

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

    public void save(GameSession session) {
        if (session == null || session.getRoomId() == null) {
            throw new IllegalArgumentException("GameSession 및 roomId는 필수입니다.");
        }
        sessions.put(session.getRoomId(), session);
    }

    public Optional<GameSession> findByRoomId(String roomId) {
        if (roomId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(roomId));
    }

    public void remove(String roomId) {
        if (roomId != null) {
            sessions.remove(roomId);
        }
    }

    public boolean exists(String roomId) {
        return roomId != null && sessions.containsKey(roomId);
    }

    public java.util.Collection<GameSession> findAll() {
        return sessions.values();
    }
}
