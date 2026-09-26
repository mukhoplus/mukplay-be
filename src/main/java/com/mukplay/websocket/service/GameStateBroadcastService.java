package com.mukplay.websocket.service;

import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.Round;
import com.mukplay.websocket.constant.StompDestination;
import com.mukplay.websocket.dto.GameStateBroadcast;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service responsible for broadcasting game state updates to STOMP subscribers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameStateBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastState(GameSession session, Round currentRound) {
        broadcastState(session, currentRound, null);
    }

    public void broadcastState(GameSession session, Round currentRound, String questionContent) {
        if (session == null) {
            throw new IllegalArgumentException("GameSession은 필수입니다.");
        }

        GameStateBroadcast message = GameStateBroadcast.from(session, currentRound, questionContent);
        String destination = StompDestination.getRoomStateDestination(session.getRoomId());

        messagingTemplate.convertAndSend(destination, message);
        log.debug("State broadcasted: destination={}, state={}, round={}, question={}",
                destination, message.state(), message.currentRound(), questionContent);
    }
}
