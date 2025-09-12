package com.jober.final2teamdrhong.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


    @RestControllerAdvice
    public class GlobalExceptionHandler {

        // 1. @Valid 검증 실패 시 발생하는 예외를 처리하는 메서드
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
            // 실패한 검증의 첫 번째 에러 메시지를 가져옴
            String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();

            // 우리가 만든 ErrorResponse DTO에 메시지를 담음
            ErrorResponse response = new ErrorResponse(errorMessage);

            // 400 Bad Request 상태 코드와 함께 응답
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
}