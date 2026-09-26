package com.mukplay.domain.game.service;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.common.exception.ErrorCode;
import com.mukplay.domain.game.dto.PlayerSettlementInfo;
import com.mukplay.domain.game.entity.GameLog;
import com.mukplay.domain.game.entity.GameResult;
import com.mukplay.domain.game.repository.GameLogRepository;
import com.mukplay.domain.game.repository.GameResultRepository;
import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to execute atomic transactional game result & EXP settlement.
 * Includes idempotent guard against duplicate settlements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameSettlementService {

    private final GameLogRepository gameLogRepository;
    private final GameResultRepository gameResultRepository;
    private final UserRepository userRepository;
    private final ExpCalculatorService expCalculatorService;

    @Transactional
    public List<GameResult> settleGame(
            String roomId,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            int participantCount,
            List<PlayerSettlementInfo> players) {

        // 1. Idempotency guard: if GameLog for this roomId already exists, return existing results
        if (gameLogRepository.findByRoomId(roomId).isPresent()) {
            GameLog existingLog = gameLogRepository.findByRoomId(roomId).get();
            log.warn("Game settlement already processed for roomId={}. Returning existing results.", roomId);
            return gameResultRepository.findByGameLogId(existingLog.getId());
        }

        // 2. Create and persist GameLog
        GameLog gameLog = GameLog.builder()
                .roomId(roomId)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .participantCount(participantCount)
                .build();
        GameLog savedLog = gameLogRepository.save(gameLog);

        List<GameResult> results = new ArrayList<>();

        // 3. For each player: calculate EXP, update user entity (with level up), persist GameResult
        for (PlayerSettlementInfo info : players) {
            int earnedExp = expCalculatorService.calculateTotalExp(
                    info.baseExpSum(),
                    info.isWinner(),
                    participantCount
            );

            // Update User exp and level if registered user (skip for bots)
            userRepository.findById(info.userId()).ifPresent(user -> {
                int oldLevel = user.getLevel();
                user.addExp(earnedExp);
                if (user.getLevel() > oldLevel) {
                    log.info("User level up! userId={}, level={} -> {}", user.getId(), oldLevel, user.getLevel());
                }
            });

            // Persist GameResult
            GameResult result = GameResult.builder()
                    .gameLogId(savedLog.getId())
                    .userId(info.userId())
                    .nickname(info.nickname())
                    .rank(info.rank())
                    .earnedExp(earnedExp)
                    .survivedRounds(info.survivedRounds())
                    .correctCount(info.correctCount())
                    .wrongCount(info.wrongCount())
                    .build();

            results.add(gameResultRepository.save(result));
        }

        return results;
    }
}
