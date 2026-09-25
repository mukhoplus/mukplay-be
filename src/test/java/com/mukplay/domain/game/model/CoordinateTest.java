package com.mukplay.domain.game.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CoordinateTest {

    @Test
    @DisplayName("정상 범위 내의 논리 좌표는 그대로 생성된다")
    void validCoordinatesWithinBounds() {
        Coordinate coordinate = new Coordinate(25.5, 75.0);

        assertThat(coordinate.x()).isEqualTo(25.5);
        assertThat(coordinate.y()).isEqualTo(75.0);
    }

    @ParameterizedTest
    @CsvSource({
            "-10.0, 50.0, 0.0, 50.0",
            "120.0, 50.0, 100.0, 50.0",
            "50.0, -5.0, 50.0, 0.0",
            "50.0, 105.0, 50.0, 100.0",
            "-1.0, 101.0, 0.0, 100.0"
    })
    @DisplayName("경계(0.0 ~ 100.0)를 벗어나는 논리 좌표는 경계값으로 clamping된다")
    void clampingOutOfBounds(double inputX, double inputY, double expectedX, double expectedY) {
        Coordinate coordinate = new Coordinate(inputX, inputY);

        assertThat(coordinate.x()).isEqualTo(expectedX);
        assertThat(coordinate.y()).isEqualTo(expectedY);
    }

    @Test
    @DisplayName("translate 수행 시 delta가 적용되며 경계 clamping이 유지된다")
    void translateWithClamping() {
        Coordinate origin = new Coordinate(98.0, 2.0);

        Coordinate moved = origin.translate(5.0, -10.0);

        assertThat(moved.x()).isEqualTo(100.0);
        assertThat(moved.y()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("isInBounds 정적 메서드로 좌표 경계 유효성을 판별할 수 있다")
    void isInBoundsCheck() {
        assertThat(Coordinate.isInBounds(0.0, 100.0)).isTrue();
        assertThat(Coordinate.isInBounds(50.0, 50.0)).isTrue();
        assertThat(Coordinate.isInBounds(-0.1, 50.0)).isFalse();
        assertThat(Coordinate.isInBounds(50.0, 100.1)).isFalse();
    }
}
