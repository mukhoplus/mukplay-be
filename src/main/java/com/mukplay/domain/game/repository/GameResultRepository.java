package com.mukplay.domain.game.repository;

import com.mukplay.domain.game.entity.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameResultRepository extends JpaRepository<GameResult, Long> {
    List<GameResult> findByGameLogId(Long gameLogId);
    List<GameResult> findByUserId(Long userId);
    Optional<GameResult> findByGameLogIdAndUserId(Long gameLogId, Long userId);
}
