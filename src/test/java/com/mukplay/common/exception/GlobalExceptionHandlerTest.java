package com.mukplay.common.exception;

import com.mukplay.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessException 발생 시 지정된 ErrorCode의 HTTP 상태와 JSON 응답을 반환해야 한다")
    void testHandleBusinessException() {
        BusinessException ex = new BusinessException(ErrorCode.ROOM_NOT_FOUND);

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getCode()).isEqualTo("R001");
        assertThat(response.getBody().getMessage()).isEqualTo("존재하지 않는 방입니다.");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("일반 Exception 발생 시 500 상태와 INTERNAL_SERVER_ERROR 코드를 반환해야 한다")
    void testHandleGeneralException() {
        RuntimeException ex = new RuntimeException("Unexpected error");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneralException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getCode()).isEqualTo("C003");
    }
}
