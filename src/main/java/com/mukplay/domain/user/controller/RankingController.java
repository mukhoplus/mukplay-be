package com.mukplay.domain.user.controller;

import com.mukplay.common.response.ApiResponse;
import com.mukplay.domain.user.dto.UserRankingResponse;
import com.mukplay.domain.user.service.UserRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final UserRankingService userRankingService;

    @GetMapping
    public ApiResponse<List<UserRankingResponse>> getRankings() {
        List<UserRankingResponse> rankings = userRankingService.getTopRankings();
        return ApiResponse.success(rankings);
    }
}
