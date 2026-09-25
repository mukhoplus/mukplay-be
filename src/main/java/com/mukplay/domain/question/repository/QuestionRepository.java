package com.mukplay.domain.question.repository;

import com.mukplay.domain.question.entity.Question;
import com.mukplay.domain.question.entity.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByStatus(QuestionStatus status);
}
