package com.mukplay.domain.game.service;

import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import org.springframework.stereotype.Service;

/**
 * Service to evaluate game completion condition:
 * 1. Alive players <= 1 (last survivor found or all eliminated)
 * 2. Max rounds reached
 */
@Service
public class GameFinishEvaluator {

    public boolean isGameFinished(GameSession session) {
        if (session == null) {
            throw new IllegalArgumentException("GameSession은 필수입니다.");
        }
        if (session.getState() == GameSessionState.FINISHED) {
            return true;
        }
        return session.shouldFinish();
    }

    public void evaluateAndFinishIfMet(GameSession session) {
        if (session == null) {
            throw new IllegalArgumentException("GameSession은 필수입니다.");
        }
        if (session.getState() == GameSessionState.FINISHED) {
            return;
        }
        if (session.shouldFinish()) {
            session.transitionTo(GameSessionState.FINISHED);
        }
    }
}
