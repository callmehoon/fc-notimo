package com.jober.final2teamdrhong.exception;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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

    // 2. 서비스 계층 등에서 비즈니스 로직상 발생하는 예외를 처리하는 메서드
    @ExceptionHandler({IllegalArgumentException.class, InvalidDataAccessApiUsageException.class})
    public ResponseEntity<ErrorResponse> handleDataRelatedExceptions(RuntimeException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 3. 위에서 처리하지 못한 모든 나머지 예외를 처리하는 최후의 보루
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        // 실제 운영에서는 여기서 에러 로그를 기록하는 것이 중요합니다 (e.g., log.error(...))
        ErrorResponse response = new ErrorResponse("서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ==================== 인증/인가 관련 예외 처리 (로그인/회원가입 전용) ====================
    
    // 4. 인증 실패 예외 처리 (로그인/회원가입 전용)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 4-1. Spring Security BadCredentialsException 처리 (컨트롤러에서 발생하는 로그인 실패)
    // SecurityConfig의 authenticationEntryPoint는 Spring Security 필터에서만 동작하므로
    // 컨트롤러 레벨에서는 GlobalExceptionHandler가 처리해야 함
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        // ex.getMessage()를 UTF-8로 재인코딩하여 한글 깨짐 문제 해결
        String message = convertToUtf8(ex.getMessage());
        ErrorResponse response = new ErrorResponse(message);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 잘못된 인코딩으로 저장된 메시지를 UTF-8로 변환하는 유틸리티 메서드
     * ISO-8859-1로 잘못 인코딩된 한글을 UTF-8로 복원
     */
    private String convertToUtf8(String message) {
        if (message == null) {
            return null;
        }

        try {
            // ISO-8859-1로 인코딩된 바이트를 UTF-8로 재해석
            byte[] bytes = message.getBytes("ISO-8859-1");
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            // 변환 실패 시 원본 메시지 반환
            return message;
        }
    }
    
    // 5. 중복 리소스 예외 처리 (회원가입 시 이메일 중복)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    // 6. 비즈니스 로직 예외 처리 (인증 코드 만료 등)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    // 7. Rate Limiting 예외 처리 (너무 많은 요청)
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
}