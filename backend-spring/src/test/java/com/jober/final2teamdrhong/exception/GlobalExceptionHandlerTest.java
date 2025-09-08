package com.jober.final2teamdrhong.exception;

import com.jober.final2teamdrhong.dto.UserSignupResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    @DisplayName("성공: Auth API에서 Validation 예외 시 UserSignupResponseDto 반환")
    void handleValidationExceptions_authApi_returnsUserSignupResponseDto() {
        // given
        when(request.getRequestURI()).thenReturn("/api/auth/signup");
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        
        FieldError fieldError = new FieldError("userSignupRequestDto", "email", "올바른 이메일 형식이 아닙니다.");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // when
        ResponseEntity<?> response = globalExceptionHandler.handleValidationExceptions(validationException, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(UserSignupResponse.class);
        
        UserSignupResponse responseDto = (UserSignupResponse) response.getBody();
        assertThat(responseDto.isSuccess()).isFalse();
        assertThat(responseDto.getMessage()).isEqualTo("올바른 이메일 형식이 아닙니다.");
    }

    @Test
    @DisplayName("성공: 일반 API에서 Validation 예외 시 ErrorResponse 반환")
    void handleValidationExceptions_generalApi_returnsErrorResponse() {
        // given
        when(request.getRequestURI()).thenReturn("/api/general/endpoint");
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        
        FieldError fieldError = new FieldError("requestDto", "name", "이름은 필수입니다.");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // when
        ResponseEntity<?> response = globalExceptionHandler.handleValidationExceptions(validationException, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertThat(errorResponse.getMessage()).isEqualTo("이름은 필수입니다.");
    }

    @Test
    @DisplayName("성공: Auth API에서 IllegalArgumentException 시 UserSignupResponseDto 반환")
    void handleIllegalArgumentException_authApi_returnsUserSignupResponseDto() {
        // given
        when(request.getRequestURI()).thenReturn("/api/auth/send-verification-code");
        IllegalArgumentException exception = new IllegalArgumentException("이메일을 입력해주세요.");

        // when
        ResponseEntity<?> response = globalExceptionHandler.handleIllegalArgumentException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(UserSignupResponse.class);
        
        UserSignupResponse responseDto = (UserSignupResponse) response.getBody();
        assertThat(responseDto.isSuccess()).isFalse();
        assertThat(responseDto.getMessage()).isEqualTo("이메일을 입력해주세요.");
    }

    @Test
    @DisplayName("성공: Auth API에서 RateLimitExceededException 시 429 상태코드와 Retry-After 헤더")
    void handleRateLimitExceededException_authApi_returns429WithRetryAfter() {
        // given
        when(request.getRequestURI()).thenReturn("/api/auth/signup");
        RateLimitExceededException exception = new RateLimitExceededException(
            "회원가입 속도 제한을 초과했습니다. 300초 후 다시 시도해주세요.", 300L);

        // when
        ResponseEntity<?> response = globalExceptionHandler.handleRateLimitExceededException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getHeaders().get("Retry-After")).contains("300");
        assertThat(response.getBody()).isInstanceOf(UserSignupResponse.class);
        
        UserSignupResponse responseDto = (UserSignupResponse) response.getBody();
        assertThat(responseDto.isSuccess()).isFalse();
        assertThat(responseDto.getMessage()).contains("300초 후 다시 시도해주세요");
    }
}
