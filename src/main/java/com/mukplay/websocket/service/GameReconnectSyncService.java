package com.mukplay.websocket.service;

import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.Round;
import com.mukplay.websocket.dto.GameSyncSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service to generate and deliver full snapshot on reconnect.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameReconnectSyncService {

    private final SimpMessagingTemplate messagingTemplate;

    public GameSyncSnapshot createSnapshot(GameSession session, Round currentRound) {
        if (session == null) {
            throw new IllegalArgumentException("GameSession은 필수입니다.");
        }
        return GameSyncSnapshot.of(session, currentRound);
    }

    public void sendSyncSnapshotToUser(String sessionId, GameSession session, Round currentRound) {
        GameSyncSnapshot snapshot = createSnapshot(session, currentRound);
        // Send directly to the reconnected client session or user queue
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/sync", snapshot);
        log.info("Sent reconnect sync snapshot: roomId={}, state={}, playerCount={}",
                session.getRoomId(), session.getState(), snapshot.positions().size());
    }
}
