package com.jober.final2teamdrhong.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSignupResponseDto {
    
    private final boolean success;
    private final String message;
    private final Object data;
    
    // 성공 응답
    public static UserSignupResponseDto success(String message) {
        return new UserSignupResponseDto(true, message, null);
    }
    
    public static UserSignupResponseDto success(String message, Object data) {
        return new UserSignupResponseDto(true, message, data);
    }
    
    // 실패 응답
    public static UserSignupResponseDto failure(String message) {
        return new UserSignupResponseDto(false, message, null);
    }
}