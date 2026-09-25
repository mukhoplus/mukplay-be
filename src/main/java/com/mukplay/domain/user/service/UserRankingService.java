package com.mukplay.domain.user.service;

import com.mukplay.domain.user.dto.UserRankingResponse;
import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRankingService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserRankingResponse> getTopRankings() {
        List<User> topUsers = userRepository.findTop100ByOrderByExpDesc();
        List<UserRankingResponse> rankings = new ArrayList<>();

        for (int i = 0; i < topUsers.size(); i++) {
            rankings.add(UserRankingResponse.of(i + 1, topUsers.get(i)));
        }

        return rankings;
    }
}
