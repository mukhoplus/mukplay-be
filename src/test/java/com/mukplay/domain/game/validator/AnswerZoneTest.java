package com.mukplay.domain.game.validator;

import com.mukplay.domain.game.model.Answer;
import com.mukplay.domain.game.model.AnswerZone;
import com.mukplay.domain.game.model.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerZoneTest {

    @ParameterizedTest
    @CsvSource({
            "0.0, O",
            "20.5, O",
            "44.999, O",
            "45.0, NEUTRAL",
            "50.0, NEUTRAL",
            "55.0, NEUTRAL",
            "55.001, X",
            "80.0, X",
            "100.0, X"
    })
    @DisplayName("X 좌표에 따라 O, X, 중립(NEUTRAL) 구역을 올바르게 판정한다")
    void determineZoneByX(double x, AnswerZone expectedZone) {
        AnswerZone zone = AnswerZoneDeterminer.determineZone(x);
        assertThat(zone).isEqualTo(expectedZone);
    }

    @Test
    @DisplayName("Coordinate 객체를 통해서도 구역을 정확히 판정한다")
    void determineZoneByCoordinate() {
        Coordinate leftCoord = new Coordinate(30.0, 50.0);
        Coordinate centerCoord = new Coordinate(50.0, 50.0);
        Coordinate rightCoord = new Coordinate(70.0, 50.0);

        assertThat(AnswerZoneDeterminer.determineZone(leftCoord)).isEqualTo(AnswerZone.O);
        assertThat(AnswerZoneDeterminer.determineZone(centerCoord)).isEqualTo(AnswerZone.NEUTRAL);
        assertThat(AnswerZoneDeterminer.determineZone(rightCoord)).isEqualTo(AnswerZone.X);
    }

    @ParameterizedTest
    @CsvSource({
            "30.0, O, true",
            "30.0, X, false",
            "70.0, X, true",
            "70.0, O, false",
            "50.0, O, false",
            "50.0, X, false"
    })
    @DisplayName("정답 영역 일치 여부를 판단하며 중립 구역은 항상 오답(false) 처리된다")
    void checkAnswerCorrectness(double x, Answer answer, boolean expectedResult) {
        boolean correct = AnswerZoneDeterminer.isCorrect(x, answer);
        assertThat(correct).isEqualTo(expectedResult);
    }
}
