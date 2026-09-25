package com.mukplay.websocket;

import com.mukplay.domain.user.entity.UserRole;
import com.mukplay.security.jwt.JwtProvider;
import com.mukplay.websocket.interceptor.JwtHandshakeInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class WebSocketAuthHandshakeTest {

    private JwtProvider jwtProvider;
    private JwtHandshakeInterceptor interceptor;

    private ServerHttpRequest request;
    private ServerHttpResponse response;
    private WebSocketHandler wsHandler;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider("mukplay-secret-key-for-test-at-least-256-bits-length-must-be-provided!", 3600000L);
        interceptor = new JwtHandshakeInterceptor(jwtProvider);

        request = mock(ServerHttpRequest.class);
        response = mock(ServerHttpResponse.class);
        wsHandler = mock(WebSocketHandler.class);
    }

    @Test
    @DisplayName("유효한 JWT가 쿼리 파라미터로 제공되면 웹소켓 핸드셰이크가 성공하고 속성이 주입된다")
    void validTokenInQueryParam_shouldPassHandshake() {
        String token = jwtProvider.createToken(42L, "mukho", UserRole.ROLE_USER);
        given(request.getHeaders()).willReturn(new HttpHeaders());
        given(request.getURI()).willReturn(URI.create("http://localhost:8080/ws/game?token=" + token));

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isTrue();
        assertThat(attributes.get(JwtHandshakeInterceptor.USER_ID_ATTR)).isEqualTo(42L);
        assertThat(attributes.get(JwtHandshakeInterceptor.LOGIN_ID_ATTR)).isEqualTo("mukho");
        assertThat(attributes.get(JwtHandshakeInterceptor.ROLE_ATTR)).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("유효한 JWT가 Authorization 헤더로 제공되면 웹소켓 핸드셰이크가 성공한다")
    void validTokenInHeader_shouldPassHandshake() {
        String token = jwtProvider.createToken(100L, "admin", UserRole.ROLE_ADMIN);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        given(request.getHeaders()).willReturn(headers);
        given(request.getURI()).willReturn(URI.create("http://localhost:8080/ws/game"));

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isTrue();
        assertThat(attributes.get(JwtHandshakeInterceptor.USER_ID_ATTR)).isEqualTo(100L);
        assertThat(attributes.get(JwtHandshakeInterceptor.LOGIN_ID_ATTR)).isEqualTo("admin");
        assertThat(attributes.get(JwtHandshakeInterceptor.ROLE_ATTR)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("토큰이 전혀 없으면 핸드셰이크가 거부된다")
    void missingToken_shouldRejectHandshake() {
        given(request.getHeaders()).willReturn(new HttpHeaders());
        given(request.getURI()).willReturn(URI.create("http://localhost:8080/ws/game"));

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isFalse();
        assertThat(attributes).isEmpty();
    }

    @Test
    @DisplayName("위조되거나 유효하지 않은 토큰이면 핸드셰이크가 거부된다")
    void invalidToken_shouldRejectHandshake() {
        given(request.getHeaders()).willReturn(new HttpHeaders());
        given(request.getURI()).willReturn(URI.create("http://localhost:8080/ws/game?token=invalid.jwt.token"));

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isFalse();
        assertThat(attributes).isEmpty();
    }

    @Test
    @DisplayName("만료된 토큰인 경우 핸드셰이크가 거부된다")
    void expiredToken_shouldRejectHandshake() {
        JwtProvider expiredJwtProvider = new JwtProvider(
                "mukplay-secret-key-for-test-at-least-256-bits-length-must-be-provided!",
                -1000L // 이미 만료
        );
        String expiredToken = expiredJwtProvider.createToken(7L, "user7", UserRole.ROLE_USER);

        given(request.getHeaders()).willReturn(new HttpHeaders());
        given(request.getURI()).willReturn(URI.create("http://localhost:8080/ws/game?token=" + expiredToken));

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isFalse();
        assertThat(attributes).isEmpty();
    }
}
