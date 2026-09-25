package com.mukplay.domain.question.service;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.common.exception.ErrorCode;
import com.mukplay.domain.question.entity.Question;
import com.mukplay.domain.question.entity.QuestionStatus;
import com.mukplay.domain.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service to select random APPROVED questions from question pool for game rounds.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionPoolSelector {

    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public List<Question> selectQuestionsForGame(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("요청 문제 수는 1 이상이어야 합니다.");
        }

        List<Question> approvedQuestions = questionRepository.findByStatus(QuestionStatus.APPROVED);

        if (approvedQuestions.size() < count) {
            log.warn("Not enough approved questions. Required={}, Available={}", count, approvedQuestions.size());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("승인된 문제가 부족합니다. (필요: %d개, 보유: %d개)", count, approvedQuestions.size()));
        }

        List<Question> shuffled = new ArrayList<>(approvedQuestions);
        Collections.shuffle(shuffled);

        return shuffled.subList(0, count);
    }
}
