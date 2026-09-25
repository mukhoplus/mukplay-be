package com.mukplay.websocket;

import com.mukplay.domain.game.command.MoveCommand;
import com.mukplay.domain.game.model.Direction;
import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.domain.game.repository.GameSessionRepository;
import com.mukplay.domain.game.service.MovementService;
import com.mukplay.websocket.controller.GameMessageController;
import com.mukplay.websocket.dto.RoomPositionsBroadcast;
import com.mukplay.websocket.service.GamePositionBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WebSocketIntegrationTest {

    private GameSessionRepository repository;
    private MovementService movementService;
    private GameMessageController messageController;
    private SimpMessagingTemplate messagingTemplate;
    private GamePositionBroadcastService positionBroadcastService;

    @BeforeEach
    void setUp() {
        repository = new GameSessionRepository();
        movementService = new MovementService();
        messageController = new GameMessageController(repository, movementService, null);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        positionBroadcastService = new GamePositionBroadcastService(messagingTemplate);
    }

    @ParameterizedTest(name = "동시 접속 클라이언트 {0}명 시뮬레이션")
    @ValueSource(ints = {2, 5, 10})
    @DisplayName("2, 5, 10명의 다중 클라이언트가 동시에 이동 명령을 전달하고 브로드캐스트를 수신한다")
    void simulateMultiClients(int clientCount) throws InterruptedException {
        String roomId = "multi-client-room-" + clientCount;
        GameSession session = new GameSession(roomId, 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        List<Long> userIds = new ArrayList<>();
        for (long i = 1; i <= clientCount; i++) {
            userIds.add(i);
            session.addPlayer(new PlayerState(i, 50.0, 50.0));
        }
        repository.save(session);

        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(clientCount);

        for (Long userId : userIds) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    // 각 클라이언트가 4회씩 이동 명령 수행
                    messageController.dispatchMove(new MoveCommand(roomId, userId, Direction.UP));
                    messageController.dispatchMove(new MoveCommand(roomId, userId, Direction.RIGHT));
                    messageController.dispatchMove(new MoveCommand(roomId, userId, Direction.DOWN));
                    messageController.dispatchMove(new MoveCommand(roomId, userId, Direction.LEFT));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 동시 출발
        startLatch.countDown();
        boolean completed = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();

        // 모든 플레이어가 안전하게 처리되었는지 확인
        for (Long userId : userIds) {
            PlayerState p = session.getPlayer(userId);
            assertThat(p).isNotNull();
            assertThat(p.getX()).isBetween(0.0, 100.0);
            assertThat(p.getY()).isBetween(0.0, 100.0);
        }

        // 위치 브로드캐스트 정상 발송 검증
        positionBroadcastService.broadcastPositions(session);

        ArgumentCaptor<RoomPositionsBroadcast> payloadCaptor = ArgumentCaptor.forClass(RoomPositionsBroadcast.class);
        verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(), payloadCaptor.capture());

        RoomPositionsBroadcast broadcast = payloadCaptor.getValue();
        assertThat(broadcast.roomId()).isEqualTo(roomId);
        assertThat(broadcast.positions()).hasSize(clientCount);
    }
}
