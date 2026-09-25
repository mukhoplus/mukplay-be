package com.mukplay.domain.game.service;

import com.mukplay.domain.game.model.Round;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Server-authoritative timer service.
 * The client timer is for presentation only; server clock strictly determines round expiration.
 */
@Service
public class RoundTimerService {

    private final Clock clock;

    public RoundTimerService() {
        this(Clock.systemUTC());
    }

    public RoundTimerService(Clock clock) {
        this.clock = clock;
    }

    public boolean isRoundEnded(Round round) {
        if (round == null) {
            throw new IllegalArgumentException("Round는 필수입니다.");
        }
        return round.isExpired(Instant.now(clock));
    }

    public Duration getRemainingTime(Round round) {
        if (round == null) {
            throw new IllegalArgumentException("Round는 필수입니다.");
        }
        return round.getRemainingDuration(Instant.now(clock));
    }

    public long getRemainingSeconds(Round round) {
        return getRemainingTime(round).toSeconds();
    }
}
