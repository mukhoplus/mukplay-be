package com.mukplay.domain.question.controller;

import com.mukplay.common.response.ApiResponse;
import com.mukplay.domain.question.dto.QuestionResponse;
import com.mukplay.domain.question.dto.QuestionSubmitRequest;
import com.mukplay.domain.question.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QuestionResponse> submitQuestion(
            @AuthenticationPrincipal Long currentUserId,
            @Valid @RequestBody QuestionSubmitRequest request) {

        Long submitterId = currentUserId != null ? currentUserId : 1L;
        QuestionResponse response = questionService.submitQuestion(submitterId, request);
        return ApiResponse.success(response);
    }
}
