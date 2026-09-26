package com.mukplay.domain.game.service;

import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.domain.game.model.Round;
import com.mukplay.domain.game.validator.AnswerZoneDeterminer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service handling round elimination logic.
 * Players located outside the correct answer zone when round ends are eliminated.
 */
@Service
public class EliminationService {

    public List<Long> processRoundElimination(GameSession session, Round round) {
        if (session == null) {
            throw new IllegalArgumentException("GameSession은 필수입니다.");
        }
        if (round == null) {
            throw new IllegalArgumentException("Round는 필수입니다.");
        }

        List<Long> eliminatedUserIds = new ArrayList<>();

        for (PlayerState player : session.getPlayers().values()) {
            if (player.isAlive()) {
                boolean correct = AnswerZoneDeterminer.isCorrect(player.getX(), round.getAnswer());
                if (correct) {
                    player.recordCorrect();
                } else {
                    player.recordWrong(session.getCurrentRound());
                    eliminatedUserIds.add(player.getUserId());
                }
            }
        }

        return eliminatedUserIds;
    }
}
