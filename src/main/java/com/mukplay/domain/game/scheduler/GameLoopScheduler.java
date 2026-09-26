package com.mukplay.domain.game.scheduler;

import com.mukplay.domain.game.dto.PlayerSettlementInfo;
import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.domain.game.model.Round;
import com.mukplay.domain.game.repository.GameSessionRepository;
import com.mukplay.domain.game.service.EliminationService;
import com.mukplay.domain.game.service.GameSettlementService;
import com.mukplay.domain.question.entity.Question;
import com.mukplay.domain.room.service.RoomService;
import com.mukplay.websocket.dto.GameEventType;
import com.mukplay.websocket.service.GameEventBroadcastService;
import com.mukplay.websocket.service.GamePositionBroadcastService;
import com.mukplay.websocket.service.GameStateBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-authoritative game loop scheduler running every 1 second.
 * Evaluates round timer expiration, answer zone correctness, elimination,
 * intermission delay, and progression to the next round or match finish.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameLoopScheduler {

    private final GameSessionRepository gameSessionRepository;
    private final RoomService roomService;
    private final EliminationService eliminationService;
    private final GameSettlementService gameSettlementService;
    private final GameStateBroadcastService gameStateBroadcastService;
    private final GamePositionBroadcastService gamePositionBroadcastService;
    private final GameEventBroadcastService gameEventBroadcastService;

    // Track when each room entered ROUND_END to allow 4 seconds for result presentation
    private final Map<String, Long> roundEndTimestamps = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 1000)
    public void tickGameLoop() {
        for (GameSession session : gameSessionRepository.findAll()) {
            try {
                handleSessionTick(session);
            } catch (Exception e) {
                log.error("Error processing game loop tick for roomId={}", session.getRoomId(), e);
            }
        }
    }

    private void handleSessionTick(GameSession session) {
        String roomId = session.getRoomId();
        Round activeRound = roomService.getActiveRound(roomId);

        // 1. 라운드 진행 중 (PLAYING) 상태 처리
        if (session.getState() == GameSessionState.PLAYING) {
            if (activeRound != null && activeRound.isExpired(Instant.now())) {
                // 라운드 종료 시간 도달!
                log.info("Round timer expired for roomId={}, round={}. Calculating elimination...",
                        roomId, session.getCurrentRound());

                session.transitionTo(GameSessionState.ROUND_END);
                roundEndTimestamps.put(roomId, System.currentTimeMillis());

                // 탈락자 판정 (정답 구역에 없는 플레이어 및 중립구역 플레이어 탈락)
                List<Long> eliminatedIds = eliminationService.processRoundElimination(session, activeRound);

                // 탈락 및 정답 결과 이벤트 브로드캐스트
                gameEventBroadcastService.broadcastEvent(roomId, GameEventType.ROUND_ENDED, Map.of(
                        "round", session.getCurrentRound(),
                        "answer", activeRound.getAnswer().name(),
                        "eliminatedUserIds", eliminatedIds,
                        "aliveCount", session.getAlivePlayerCount()
                ));

                // 탈락된 플레이어 상태(💀) 화면에 반영되도록 위치 브로드캐스트
                gamePositionBroadcastService.broadcastPositions(session);

                log.info("Round ended: roomId={}, answer={}, eliminatedCount={}",
                        roomId, activeRound.getAnswer(), eliminatedIds.size());
            }
            return;
        }

        // 2. 라운드 종료 후 대기 중 (ROUND_END) 상태 처리
        if (session.getState() == GameSessionState.ROUND_END) {
            Long endedAt = roundEndTimestamps.get(roomId);
            if (endedAt == null) {
                roundEndTimestamps.put(roomId, System.currentTimeMillis());
                return;
            }

            long elapsed = System.currentTimeMillis() - endedAt;
            // 4초간 정답과 탈락 결과 확인 대기
            if (elapsed >= 4000) {
                roundEndTimestamps.remove(roomId);

                // 게임 종료 조건 충족 여부 검사 (생존자 1명 이하 또는 5라운드 완료)
                if (session.shouldFinish()) {
                    session.transitionTo(GameSessionState.FINISHED);
                    log.info("Game finished for roomId={}, aliveCount={}", roomId, session.getAlivePlayerCount());

                    // 게임 결과 및 경험치 정산
                    try {
                        List<PlayerState> allPlayers = new ArrayList<>(session.getPlayers().values());
                        allPlayers.sort((a, b) -> {
                            if (a.isAlive() != b.isAlive()) {
                                return a.isAlive() ? -1 : 1;
                            }
                            if (b.getSurvivedRounds() != a.getSurvivedRounds()) {
                                return Integer.compare(b.getSurvivedRounds(), a.getSurvivedRounds());
                            }
                            return Integer.compare(b.getCorrectCount(), a.getCorrectCount());
                        });

                        List<PlayerSettlementInfo> settlementInfos = new ArrayList<>();
                        int currentRank = 1;
                        for (int i = 0; i < allPlayers.size(); i++) {
                            PlayerState p = allPlayers.get(i);
                            boolean isWinner = p.isAlive() && (i == 0 || allPlayers.get(0).isAlive());
                            int baseExp = p.getSurvivedRounds() * 20;

                            settlementInfos.add(new PlayerSettlementInfo(
                                    p.getUserId(),
                                    p.getNickname(),
                                    currentRank,
                                    p.getSurvivedRounds(),
                                    p.getCorrectCount(),
                                    p.getWrongCount(),
                                    baseExp,
                                    isWinner
                            ));
                            currentRank++;
                        }

                        gameSettlementService.settleGame(
                                roomId,
                                LocalDateTime.ofInstant(session.getStartedAt(), ZoneId.systemDefault()),
                                LocalDateTime.now(),
                                allPlayers.size(),
                                settlementInfos
                        );
                        log.info("Game settlement completed successfully for roomId={}", roomId);
                    } catch (Exception e) {
                        log.error("Failed to settle game for roomId={}", roomId, e);
                    }

                    gameEventBroadcastService.broadcastEvent(roomId, GameEventType.GAME_FINISHED, Map.of(
                            "roomId", roomId,
                            "survivorCount", session.getAlivePlayerCount()
                    ));

                    gameStateBroadcastService.broadcastState(session, activeRound, "게임이 종료되었습니다! 잠시 후 결과창으로 이동합니다.");
                } else {
                    // 다음 라운드로 진행
                    session.nextRound();
                    Question nextQuestion = roomService.getRandomQuestion();
                    Round nextRound = new Round(
                            session.getCurrentRound(),
                            nextQuestion.getId(),
                            nextQuestion.getAnswer(),
                            Instant.now(),
                            Duration.ofSeconds(15)
                    );

                    roomService.updateActiveRound(roomId, nextRound, nextQuestion.getContent());
                    session.transitionTo(GameSessionState.PLAYING);

                    log.info("Starting next round: roomId={}, round={}, question={}",
                            roomId, session.getCurrentRound(), nextQuestion.getContent());

                    // 새 라운드 시작 브로드캐스트
                    gameStateBroadcastService.broadcastState(session, nextRound, nextQuestion.getContent());
                    gameEventBroadcastService.broadcastEvent(roomId, GameEventType.ROUND_STARTED, Map.of(
                            "round", session.getCurrentRound(),
                            "questionContent", nextQuestion.getContent()
                    ));
                    gamePositionBroadcastService.broadcastPositions(session);
                }
            }
        }
    }
}
