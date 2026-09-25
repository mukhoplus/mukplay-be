package com.mukplay.domain.game.concurrency;

import com.mukplay.domain.game.model.Direction;
import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.domain.game.service.MovementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrencyTest {

    private final MovementService movementService = new MovementService();

    @Test
    @DisplayName("[동시성 검증 1: 다중 플레이어 동시 이동] 여러 스레드가 동시에 서로 다른 플레이어를 이동시킬 때 상태가 일관성 있게 유지된다")
    void testConcurrentPlayerMovement() throws InterruptedException {
        int playerCount = 20;
        int movesPerPlayer = 50;
        String roomId = "concurrency-room-1";

        GameSession session = new GameSession(roomId, 10);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        List<PlayerState> players = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            PlayerState player = new PlayerState((long) i, 50.0, 50.0);
            session.addPlayer(player);
            players.add(player);
        }

        ExecutorService executor = Executors.newFixedThreadPool(playerCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(playerCount);

        for (int i = 0; i < playerCount; i++) {
            final PlayerState player = players.get(i);
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int m = 0; m < movesPerPlayer; m++) {
                        movementService.move(session, player, Direction.RIGHT);
                    }
                } catch (Exception e) {
                    // ignore
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        for (PlayerState player : players) {
            assertThat(player.getX()).isGreaterThan(50.0);
            assertThat(player.isAlive()).isTrue();
        }
    }

    @Test
    @DisplayName("[동시성 검증 2: 이동 중 동시 탈락 레이스 컨디션] 라운드 판정으로 플레이어가 탈락되는 도중 이동 요청이 인입될 때 안전하게 예외 처리된다")
    void testRaceConditionMoveAndEliminate() throws InterruptedException {
        String roomId = "concurrency-room-2";
        GameSession session = new GameSession(roomId, 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState player = new PlayerState(99L, 50.0, 50.0);
        session.addPlayer(player);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount + 1);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount + 1);
        AtomicInteger successMoves = new AtomicInteger(0);
        AtomicInteger rejectedMoves = new AtomicInteger(0);

        // 10개의 이동 요청 스레드
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    movementService.move(session, player, Direction.UP);
                    successMoves.incrementAndGet();
                } catch (IllegalStateException e) {
                    rejectedMoves.incrementAndGet();
                } catch (Exception e) {
                    // other errors
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 1개의 동시 탈락 스레드
        executor.submit(() -> {
            try {
                startLatch.await();
                player.eliminate();
            } catch (Exception e) {
                // ignore
            } finally {
                doneLatch.countDown();
            }
        });

        startLatch.countDown();
        boolean completed = doneLatch.await(3, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(player.isAlive()).isFalse();
        // 이동 성공 수와 거절된 수의 합이 전체 시도 횟수(10)여야 함
        assertThat(successMoves.get() + rejectedMoves.get()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("[동시성 검증 3: 동시 참가자 등록 스레드 안전성] 여러 스레드가 동시에 세션에 플레이어를 등록해도 누락 없이 카운트된다")
    void testConcurrentPlayerJoin() throws InterruptedException {
        int threads = 30;
        GameSession session = new GameSession("concurrency-room-3", 5);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final long userId = i + 1;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    session.addPlayer(new PlayerState(userId, 50.0, 50.0));
                } catch (Exception e) {
                    // ignore
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(3, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(session.getAlivePlayerCount()).isEqualTo(threads);
        assertThat(session.getPlayers().size()).isEqualTo(threads);
    }
}
