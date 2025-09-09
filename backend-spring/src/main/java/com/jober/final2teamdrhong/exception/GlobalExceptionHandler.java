package com.jober.final2teamdrhong.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 컨트롤러 전역에서 발생한 예외를 표준화된 오류 응답으로 변환하는 핸들러.
 * 인증/인가 실패, 데이터베이스 접근 오류, 미처리 예외를 {@link ErrorResponse}로 매핑하고
 * 상황에 맞는 HTTP 상태 코드를 설정하여 반환한다.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 인증 실패 예외를 처리한다. (예: 토큰 누락/만료/위조)
     *
     * @param ex 인증 관련 예외
     * @return 401 Unauthorized와 함께 사용자 안내 메시지를 담은 {@link ErrorResponse}
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("인증 실패: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("로그인이 필요합니다. 다시 로그인해주세요.");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 권한 부족으로 접근이 거부될 때의 예외를 처리한다. (예: 필요한 역할/권한 없음)
     *
     * @param ex 접근 거부 예외
     * @return 403 Forbidden과 함께 사용자 안내 메시지를 담은 {@link ErrorResponse}
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("접근 권한 없음: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("접근 권한이 없습니다.");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * @Valid 검증 실패 시 발생하는 예외를 처리한다.
     *
     * @param ex 검증 실패 예외
     * @return 400 Bad Request와 함께 검증 오류 메시지를 담은 {@link ErrorResponse}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("요청 파라미터 검증 실패: {}", ex.getMessage());
        
        // 첫 번째 검증 오류 메시지를 사용
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("요청 파라미터가 올바르지 않습니다.");
        
        ErrorResponse response = new ErrorResponse(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 데이터베이스 접근 중 발생한 예외를 처리한다.
     *
     * @param ex 데이터 접근 예외
     * @return 500 Internal Server Error와 함께 일반적인 안내 메시지를 담은 {@link ErrorResponse}
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
        log.error("데이터베이스 접근 오류 발생: {}", ex.getMessage(), ex);
        ErrorResponse response = new ErrorResponse("서비스를 일시적으로 이용할 수 없습니다. 잠시 후 다시 시도해주세요.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 위에서 처리하지 못한 모든 예외를 포괄적으로 처리한다.
     *
     * @param ex 예상치 못한 예외
     * @return 500 Internal Server Error와 함께 일반적인 안내 메시지를 담은 {@link ErrorResponse}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("예상치 못한 서버 오류 발생: {}", ex.getMessage(), ex);
        ErrorResponse response = new ErrorResponse("서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}