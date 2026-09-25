package com.mukplay.websocket;

import com.mukplay.domain.game.command.MoveCommand;
import com.mukplay.domain.game.model.Direction;
import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.domain.game.repository.GameSessionRepository;
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

class MoveCommandHandlerTest {

    private GameSessionRepository repository;
    private MovementService movementService;
    private GameMessageController controller;

    @BeforeEach
    void setUp() {
        repository = new GameSessionRepository();
        movementService = new MovementService();
        controller = new GameMessageController(repository, movementService, null);
    }

    @Test
    @DisplayName("STOMP MoveCommand 요청 수신 시 GameSession의 해당 플레이어 위치가 서버 권위적으로 이동한다")
    void handleMoveCommandRoutesToGameSession() {
        String roomId = "room-ws-1";
        Long userId = 77L;

        // 세션 및 플레이어 초기화
        GameSession session = new GameSession(roomId, 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState player = new PlayerState(userId, 50.0, 50.0);
        session.addPlayer(player);
        repository.save(session);

        // HeaderAccessor Mocking
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put(JwtHandshakeInterceptor.USER_ID_ATTR, userId);
        headerAccessor.setSessionAttributes(sessionAttributes);

        MoveRequest request = new MoveRequest(roomId, Direction.UP);
        controller.handleMove(request, headerAccessor);

        // 검증: 플레이어가 위로 이동했는지 확인
        assertThat(player.getY()).isEqualTo(50.0 - MovementService.STEP_SIZE);
        assertThat(player.getX()).isEqualTo(50.0);

        // 오른쪽으로 한번 더 이동
        controller.handleMove(new MoveRequest(roomId, Direction.RIGHT), headerAccessor);
        assertThat(player.getX()).isEqualTo(50.0 + MovementService.STEP_SIZE);
    }

    @Test
    @DisplayName("직접 dispatchMove 호출 시에도 GameSession을 찾아 정상 이동한다")
    void directDispatchMove() {
        String roomId = "room-ws-2";
        Long userId = 88L;

        GameSession session = new GameSession(roomId, 3);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState player = new PlayerState(userId, 10.0, 20.0);
        session.addPlayer(player);
        repository.save(session);

        MoveCommand command = new MoveCommand(roomId, userId, Direction.DOWN);
        controller.dispatchMove(command);

        assertThat(player.getY()).isEqualTo(20.0 + MovementService.STEP_SIZE);
    }
}
