package com.mukplay.domain.game.service;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.common.exception.ErrorCode;
import com.mukplay.domain.game.dto.GameResultResponse;
import com.mukplay.domain.game.dto.GameResultResponse.PlayerResultDto;
import com.mukplay.domain.game.entity.GameLog;
import com.mukplay.domain.game.entity.GameResult;
import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.domain.game.repository.GameLogRepository;
import com.mukplay.domain.game.repository.GameResultRepository;
import com.mukplay.domain.game.repository.GameSessionRepository;
import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameResultQueryService {

    private final GameLogRepository gameLogRepository;
    private final GameResultRepository gameResultRepository;
    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public GameResultResponse getGameResult(String roomId, Long currentUserId) {
        // 1. Try fetching persisted GameLog and GameResults from DB
        Optional<GameLog> gameLogOpt = gameLogRepository.findByRoomId(roomId);
        if (gameLogOpt.isPresent()) {
            GameLog log = gameLogOpt.get();
            List<GameResult> results = gameResultRepository.findByGameLogId(log.getId());
            results.sort(Comparator.comparingInt(GameResult::getRank));

            List<PlayerResultDto> dtoList = new ArrayList<>();
            for (GameResult res : results) {
                User user = userRepository.findById(res.getUserId()).orElse(null);
                int level = user != null ? user.getLevel() : 1;
                int exp = user != null ? user.getExp() : 0;
                int nextLevelExp = level * 100;
                int progressPercent = Math.min(100, Math.max(0, exp % 100));

                dtoList.add(new PlayerResultDto(
                        res.getUserId(),
                        res.getNickname() != null ? res.getNickname() : "플레이어 " + res.getUserId(),
                        res.getRank(),
                        res.getCorrectCount(),
                        res.getWrongCount(),
                        res.getSurvivedRounds(),
                        res.getEarnedExp(),
                        level,
                        exp,
                        nextLevelExp,
                        progressPercent,
                        res.getRank() == 1
                ));
            }

            PlayerResultDto myDto = dtoList.stream()
                    .filter(d -> d.userId().equals(currentUserId))
                    .findFirst()
                    .orElse(!dtoList.isEmpty() ? dtoList.get(0) : null);

            return new GameResultResponse(roomId, log.getParticipantCount(), myDto, dtoList);
        }

        // 2. Fallback to active in-memory GameSession if DB log is not yet created
        Optional<GameSession> sessionOpt = gameSessionRepository.findByRoomId(roomId);
        if (sessionOpt.isPresent()) {
            GameSession session = sessionOpt.get();
            List<PlayerState> players = new ArrayList<>(session.getPlayers().values());
            players.sort((a, b) -> {
                if (a.isAlive() != b.isAlive()) return a.isAlive() ? -1 : 1;
                if (b.getSurvivedRounds() != a.getSurvivedRounds()) {
                    return Integer.compare(b.getSurvivedRounds(), a.getSurvivedRounds());
                }
                return Integer.compare(b.getCorrectCount(), a.getCorrectCount());
            });

            List<PlayerResultDto> dtoList = new ArrayList<>();
            int rank = 1;
            for (PlayerState p : players) {
                User user = userRepository.findById(p.getUserId()).orElse(null);
                int level = user != null ? user.getLevel() : 1;
                int exp = user != null ? user.getExp() : 0;
                int nextLevelExp = level * 100;
                int progressPercent = Math.min(100, Math.max(0, exp % 100));
                int earnedExp = p.getSurvivedRounds() * 20;

                dtoList.add(new PlayerResultDto(
                        p.getUserId(),
                        p.getNickname(),
                        rank,
                        p.getCorrectCount(),
                        p.getWrongCount(),
                        p.getSurvivedRounds(),
                        earnedExp,
                        level,
                        exp,
                        nextLevelExp,
                        progressPercent,
                        rank == 1
                ));
                rank++;
            }

            PlayerResultDto myDto = dtoList.stream()
                    .filter(d -> d.userId().equals(currentUserId))
                    .findFirst()
                    .orElse(!dtoList.isEmpty() ? dtoList.get(0) : null);

            return new GameResultResponse(roomId, players.size(), myDto, dtoList);
        }

        throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "게임 결과를 찾을 수 없습니다. roomId=" + roomId);
    }
}
