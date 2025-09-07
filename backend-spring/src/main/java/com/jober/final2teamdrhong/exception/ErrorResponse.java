package com.jober.final2teamdrhong.exception;

import lombok.Getter;

// 에러 응답을 위한 간단한 DTO 클래스
@Getter
public class ErrorResponse {
    private final String message;
    
    // Rate Limiting 전용 필드 (선택적으로 포함)
    private final Long retryAfterSeconds;

    // 기본 에러 응답용 생성자
    public ErrorResponse(String message) {
        this.message = message;
        this.retryAfterSeconds = null;
    }
    
    // Rate Limiting 에러 응답용 생성자 (retryAfterSeconds 포함)
    public ErrorResponse(String message, Long retryAfterSeconds) {
        this.message = message;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}