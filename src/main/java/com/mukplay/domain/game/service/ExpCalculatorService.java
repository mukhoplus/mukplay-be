package com.mukplay.domain.game.service;

import com.mukplay.domain.question.entity.QuestionDifficulty;
import org.springframework.stereotype.Service;

/**
 * Service to calculate EXP rewards according to the game formula:
 * - Base round EXP: HARD/HIGH=30, NORMAL/MID=20, EASY/LOW=10
 * - Winner bonus EXP: (initialParticipants - 1) * 10
 */
@Service
public class ExpCalculatorService {

    public static final int EXP_HIGH = 30;
    public static final int EXP_MID = 20;
    public static final int EXP_LOW = 10;
    public static final int WINNER_MULTIPLIER = 10;

    public int calculateRoundExp(QuestionDifficulty difficulty) {
        if (difficulty == null) {
            return EXP_MID;
        }
        return switch (difficulty) {
            case HARD -> EXP_HIGH;
            case NORMAL -> EXP_MID;
            case EASY -> EXP_LOW;
        };
    }

    public int calculateWinnerBonus(int initialParticipants) {
        if (initialParticipants <= 1) {
            return 0;
        }
        return (initialParticipants - 1) * WINNER_MULTIPLIER;
    }

    public int calculateTotalExp(int survivedRoundsExpSum, boolean isWinner, int initialParticipants) {
        int bonus = isWinner ? calculateWinnerBonus(initialParticipants) : 0;
        return survivedRoundsExpSum + bonus;
    }
}
