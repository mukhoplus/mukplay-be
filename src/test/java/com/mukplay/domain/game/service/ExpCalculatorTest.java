package com.mukplay.domain.game.service;

import com.mukplay.domain.question.entity.QuestionDifficulty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ExpCalculatorTest {

    private final ExpCalculatorService expCalculator = new ExpCalculatorService();

    @ParameterizedTest
    @CsvSource({
            "HARD, 30",
            "NORMAL, 20",
            "EASY, 10"
    })
    @DisplayName("라운드 통과 시 난이도별 기본 경험치가 올바르게 부여된다 (HARD=30, NORMAL=20, EASY=10)")
    void calculateRoundExp(QuestionDifficulty difficulty, int expectedExp) {
        int exp = expCalculator.calculateRoundExp(difficulty);
        assertThat(exp).isEqualTo(expectedExp);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 0",
            "2, 10",
            "5, 40",
            "10, 90"
    })
    @DisplayName("승리자 보너스 경험치는 (초기 참가자 수 - 1) * 10 으로 계산된다")
    void calculateWinnerBonus(int initialParticipants, int expectedBonus) {
        int bonus = expCalculator.calculateWinnerBonus(initialParticipants);
        assertThat(bonus).isEqualTo(expectedBonus);
    }

    @Test
    @DisplayName("최종 획득 경험치 합산 (통과 라운드 경험치 합 + 승자 보너스)이 정확하다")
    void calculateTotalExp() {
        // 5명 참가 게임에서 2라운드(NORMAL: 20, HARD: 30) 통과 후 우승한 경우:
        // 통과 합: 50, 승자 보너스: (5 - 1) * 10 = 40 => 총 90
        int totalExp = expCalculator.calculateTotalExp(50, true, 5);
        assertThat(totalExp).isEqualTo(90);

        // 5명 참가 게임에서 2라운드 통과 후 탈락(패배)한 경우:
        // 승자 보너스 0 => 총 50
        int loserExp = expCalculator.calculateTotalExp(50, false, 5);
        assertThat(loserExp).isEqualTo(50);
    }
}
