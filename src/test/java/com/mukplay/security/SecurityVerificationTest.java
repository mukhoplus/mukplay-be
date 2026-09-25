package com.mukplay.security;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.domain.game.model.Direction;
import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.domain.game.repository.GameSessionRepository;
import com.mukplay.domain.game.service.MovementRateLimiter;
import com.mukplay.domain.game.service.MovementService;
import com.mukplay.websocket.controller.GameMessageController;
import com.mukplay.websocket.dto.MoveRequest;
import com.mukplay.websocket.interceptor.JwtHandshakeInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SecurityVerificationTest {

    private GameSessionRepository repository;
    private MovementService movementService;
    private MovementRateLimiter rateLimiter;
    private GameMessageController controller;

    @BeforeEach
    void setUp() {
        repository = new GameSessionRepository();
        movementService = new MovementService();
        rateLimiter = mock(MovementRateLimiter.class);
        controller = new GameMessageController(repository, movementService, rateLimiter);
    }

    @Test
    @DisplayName("[보안 검증 1: 사용자 스푸핑 방지] 세션 인증 정보(userId)가 없는 STOMP 이동 요청은 무시/차단된다")
    void testUnauthenticatedUserCannotMove() {
        String roomId = "sec-room-1";
        GameSession session = new GameSession(roomId, 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);
        PlayerState player = new PlayerState(100L, 50.0, 50.0);
        session.addPlayer(player);
        repository.save(session);

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        // 세션 속성에 userId 없음
        headerAccessor.setSessionAttributes(new HashMap<>());

        MoveRequest request = new MoveRequest(roomId, Direction.UP);
        controller.handleMove(request, headerAccessor);

        // 위치 변화가 없어야 함
        assertThat(player.getY()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("[보안 검증 2: 도배/스팸 차단] RateLimiter 한도를 초과한 고빈도 이동 요청은 서버에서 무시된다")
    void testRateLimitExceededBlocksMove() {
        String roomId = "sec-room-2";
        Long userId = 200L;
        GameSession session = new GameSession(roomId, 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);
        PlayerState player = new PlayerState(userId, 50.0, 50.0);
        session.addPlayer(player);
        repository.save(session);

        when(rateLimiter.isAllowed(userId)).thenReturn(false);

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put(JwtHandshakeInterceptor.USER_ID_ATTR, userId);
        headerAccessor.setSessionAttributes(sessionAttributes);

        MoveRequest request = new MoveRequest(roomId, Direction.UP);
        controller.handleMove(request, headerAccessor);

        // 레이트 리밋 차단으로 인해 이동하지 않아야 함
        assertThat(player.getY()).isEqualTo(50.0);
        verify(rateLimiter, times(1)).isAllowed(userId);
    }

    @Test
    @DisplayName("[보안 검증 3: 크로스 룸 조작 방지] 다른 방 참가자가 해당 방의 세션 조작을 시도하면 예외가 발생한다")
    void testCrossRoomSpoofingThrowsException() {
        String roomA = "room-A";
        String roomB = "room-B";
        Long attackerId = 666L;

        // room A 세션 생성 (공격자는 참가하지 않음)
        GameSession sessionA = new GameSession(roomA, 5);
        sessionA.transitionTo(GameSessionState.STARTING);
        sessionA.transitionTo(GameSessionState.PLAYING);
        PlayerState legitimatePlayer = new PlayerState(111L, 50.0, 50.0);
        sessionA.addPlayer(legitimatePlayer);
        repository.save(sessionA);

        // 공격자 세션 속성
        when(rateLimiter.isAllowed(attackerId)).thenReturn(true);
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put(JwtHandshakeInterceptor.USER_ID_ATTR, attackerId);
        headerAccessor.setSessionAttributes(sessionAttributes);

        // 공격자가 참가하지 않은 roomA에 이동 요청 전송
        MoveRequest attackRequest = new MoveRequest(roomA, Direction.UP);

        assertThatThrownBy(() -> controller.handleMove(attackRequest, headerAccessor))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("[보안 검증 4: 탈락자 치트 방지] 이미 탈락한 플레이어는 이동 조작이 원천 차단된다")
    void testEliminatedPlayerCannotMove() {
        String roomId = "sec-room-3";
        Long eliminatedUserId = 300L;

        GameSession session = new GameSession(roomId, 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState player = new PlayerState(eliminatedUserId, 50.0, 50.0);
        player.eliminate(); // 탈락 처리
        session.addPlayer(player);
        repository.save(session);

        when(rateLimiter.isAllowed(eliminatedUserId)).thenReturn(true);
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put(JwtHandshakeInterceptor.USER_ID_ATTR, eliminatedUserId);
        headerAccessor.setSessionAttributes(sessionAttributes);

        MoveRequest request = new MoveRequest(roomId, Direction.UP);

        assertThatThrownBy(() -> controller.handleMove(request, headerAccessor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("탈락한 플레이어는 이동할 수 없습니다");
    }
}
