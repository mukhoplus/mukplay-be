package com.mukplay.security.jwt;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("JWT 토큰 생성 후 claims를 정상적으로 파싱할 수 있어야 한다")
    void testCreateAndParseToken() {
        String token = jwtProvider.createToken(1L, "player1", UserRole.ROLE_USER);

        assertThat(token).isNotBlank();
        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getUserId(token)).isEqualTo(1L);
        assertThat(jwtProvider.getLoginId(token)).isEqualTo("player1");
        assertThat(jwtProvider.getRole(token)).isEqualTo(UserRole.ROLE_USER);
    }

    @Test
    @DisplayName("변조된 토큰은 유효성 검증에 실패하고 예외를 던져야 한다")
    void testTamperedToken() {
        String token = jwtProvider.createToken(1L, "player1", UserRole.ROLE_USER);
        String tamperedToken = token + "xyz";

        assertThat(jwtProvider.validateToken(tamperedToken)).isFalse();
        assertThatThrownBy(() -> jwtProvider.parseClaims(tamperedToken))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("만료된 토큰은 유효성 검증에 실패해야 한다")
    void testExpiredToken() {
        // Create an expired provider with -1000ms expiration
        JwtProvider expiredProvider = new JwtProvider(
                "mukplay-super-secure-jwt-secret-key-for-ox-quiz-game-2026-very-long-key",
                -1000
        );
        String expiredToken = expiredProvider.createToken(2L, "expiredUser", UserRole.ROLE_USER);

        assertThat(expiredProvider.validateToken(expiredToken)).isFalse();
        assertThatThrownBy(() -> expiredProvider.parseClaims(expiredToken))
                .isInstanceOf(BusinessException.class);
    }
}
