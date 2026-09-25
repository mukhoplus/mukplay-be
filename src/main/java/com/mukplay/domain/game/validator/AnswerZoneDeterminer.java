package com.mukplay.domain.game.validator;

import com.mukplay.domain.game.model.Answer;
import com.mukplay.domain.game.model.AnswerZone;
import com.mukplay.domain.game.model.Coordinate;

/**
 * Determines answer zones and correctness based on normalized coordinates.
 * Rules:
 *  - x < 45.0  -> O zone
 *  - x > 55.0  -> X zone
 *  - 45.0 <= x <= 55.0 -> NEUTRAL zone (invalid / unconfirmed)
 */
public class AnswerZoneDeterminer {

    public static final double O_ZONE_MAX_X = 45.0;
    public static final double X_ZONE_MIN_X = 55.0;

    public static AnswerZone determineZone(double x) {
        if (x < O_ZONE_MAX_X) {
            return AnswerZone.O;
        } else if (x > X_ZONE_MIN_X) {
            return AnswerZone.X;
        } else {
            return AnswerZone.NEUTRAL;
        }
    }

    public static AnswerZone determineZone(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate는 필수입니다.");
        }
        return determineZone(coordinate.x());
    }

    public static boolean isCorrect(double x, Answer correctAnswer) {
        if (correctAnswer == null) {
            throw new IllegalArgumentException("정답은 필수입니다.");
        }
        AnswerZone zone = determineZone(x);
        return switch (zone) {
            case O -> correctAnswer == Answer.O;
            case X -> correctAnswer == Answer.X;
            case NEUTRAL -> false;
        };
    }
}
