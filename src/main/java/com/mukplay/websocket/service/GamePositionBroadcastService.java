package com.mukplay.websocket.service;

import com.mukplay.domain.game.model.GameSession;
import com.mukplay.websocket.constant.StompDestination;
import com.mukplay.websocket.dto.RoomPositionsBroadcast;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service responsible for broadcasting player positions on tick/movement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GamePositionBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastPositions(GameSession session) {
        if (session == null) {
            throw new IllegalArgumentException("GameSession은 필수입니다.");
        }

        RoomPositionsBroadcast payload = RoomPositionsBroadcast.from(session);
        String destination = StompDestination.getRoomPositionsDestination(session.getRoomId());

        messagingTemplate.convertAndSend(destination, payload);
        log.debug("Positions broadcasted: destination={}, playerCount={}",
                destination, payload.positions().size());
    }
}
