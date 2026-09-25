package com.mukplay.domain.game.repository;

import com.mukplay.domain.game.entity.GameLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameLogRepository extends JpaRepository<GameLog, Long> {
    Optional<GameLog> findByRoomId(String roomId);
}
